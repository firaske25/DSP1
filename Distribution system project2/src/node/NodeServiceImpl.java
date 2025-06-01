package node;

import java.io.*;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Files;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.ConcurrentHashMap;


public class NodeServiceImpl extends UnicastRemoteObject implements NodeService {

    private final File baseDir;
    private final ConcurrentHashMap<String, ReentrantReadWriteLock> fileLocks = new ConcurrentHashMap<>();

    public NodeServiceImpl(String basePath) throws RemoteException {
        super();
        this.baseDir = new File(basePath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }

        // إنشاء مجلدات الأقسام إذا لم تكن موجودة
        for (String dept : new String[]{"development", "QA", "design"}) {
            File deptDir = new File(baseDir, dept);
            if (!deptDir.exists()) {
                deptDir.mkdirs();
            }
        }
    }

    private File getFilePath(String department, String filename) {
        return new File(baseDir, department + File.separator + filename);
    }

    @Override
    public boolean uploadFile(String department, String filename, byte[] fileData) throws RemoteException {
        File file = getFilePath(department, filename);
        ReentrantReadWriteLock lock = getLock(file.getAbsolutePath()); // الحصول على القفل لهذا الملف
        lock.writeLock().lock(); // قفل للكتابة (لمنع القراءة أو الكتابة من خيوط أخرى)

        try {
            if (file.exists()) {
                return false; // الملف موجود بالفعل
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock(); // فك القفل في جميع الحالات
        }
    }

    @Override
    public boolean updateFile(String department, String filename, byte[] fileData) throws RemoteException {
        File file = getFilePath(department, filename);
        ReentrantReadWriteLock lock = getLock(file.getAbsolutePath());
        lock.writeLock().lock(); // قفل للكتابة

        try {
            if (!file.exists()) {
                return false; // الملف غير موجود
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Update failed: " + e.getMessage());
            return false;
        } finally {
            lock.writeLock().unlock(); // فك القفل دائماً
        }
    }

    @Override
    public boolean deleteFile(String department, String filename) throws RemoteException {
        File file = getFilePath(department, filename);
        ReentrantReadWriteLock lock = getLock(file.getAbsolutePath());
        lock.writeLock().lock(); // قفل للكتابة

        try {
            return file.exists() && file.delete();
        } finally {
            lock.writeLock().unlock(); // فك القفل دائماً
        }
    }

    @Override
    public List<String> listFiles(String department) throws RemoteException {
        File deptDir = new File(baseDir, department);
        List<String> files = new ArrayList<>();
        if (deptDir.exists() && deptDir.isDirectory()) {
            File[] fileList = deptDir.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                    if (f.isFile()) {
                        files.add(f.getName());
                    }
                }
            }
        }
        return files;
    }

    @Override
    public byte[] downloadFile(String department, String filename) throws RemoteException {
        File file = new File(baseDir + "/" + department + "/" + filename);
        if (!file.exists()) return null;

        try {
            return Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public byte[] getFileContent(String department, String filename) throws RemoteException {
        String filePath = baseDir + "/" + department + "/" + filename;
        ReentrantReadWriteLock lock = getLock(filePath);
        lock.readLock().lock();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return Files.readAllBytes(file.toPath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }


    private ReentrantReadWriteLock getLock(String filepath) {
        return fileLocks.computeIfAbsent(filepath, k -> new ReentrantReadWriteLock());
    }
}

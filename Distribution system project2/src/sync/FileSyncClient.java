package sync;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class FileSyncClient {
    private String baseDir;
    private List<String> otherNodes; // قائمة بالعناوين للعقد الأخرى

    public FileSyncClient(String baseDir, List<String> otherNodes) {
        this.baseDir = baseDir;
        this.otherNodes = otherNodes;
    }

    public void syncAllDepartments() {
        for (String dept : new String[]{"development", "QA", "design"}) {
            File deptDir = new File(baseDir, dept);
            if (deptDir.exists() && deptDir.isDirectory()) {
                File[] files = deptDir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile()) {
                            for (String nodeAddress : otherNodes) {
                                sendFileToNode(f, dept, nodeAddress);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendFileToNode(File file, String department, String nodeAddress) {
        String[] parts = nodeAddress.split(":");
        String host = parts[0];
        int port = Integer.parseInt(parts[1]);

        try (Socket socket = new Socket(host, port);
             DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
             FileInputStream fis = new FileInputStream(file)) {

            dos.writeUTF(department);
            dos.writeUTF(file.getName());
            dos.writeLong(file.length());

            byte[] buffer = new byte[4096];
            int bytes;
            while ((bytes = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytes);
            }

            System.out.println("File " + file.getName() + " sent to " + nodeAddress);
        } catch (IOException e) {
            System.out.println("Error sending file to node " + nodeAddress);
            e.printStackTrace();
        }
    }
}

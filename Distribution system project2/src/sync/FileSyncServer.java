package sync;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileSyncServer {
    private String baseDir;
    private int port;

    public FileSyncServer(String baseDir, int port) {
        this.baseDir = baseDir;
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("FileSyncServer listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
            String department = dis.readUTF();
            String filename = dis.readUTF();
            long fileSize = dis.readLong();

            File deptDir = new File(baseDir, department);
            if (!deptDir.exists()) deptDir.mkdirs();

            File targetFile = new File(deptDir, filename);
            if (targetFile.exists()) {
                System.out.println("File " + filename + " already exists, skipping.");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                int bytes;
                while (totalRead < fileSize && (bytes = dis.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytes);
                    totalRead += bytes;
                }
            }

            System.out.println("Received file " + filename + " in department " + department);

        } catch (IOException e) {
            System.out.println("Error receiving file:");
            e.printStackTrace();
        }
    }
}

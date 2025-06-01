package test;

import coordinator.CoordinatorService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ConcurrencyTest {
    public static void main(String[] args) {
        try {
            // الاتصال بالـ Coordinator
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) registry.lookup("CoordinatorService");

            String token = coordinator.loginUser("bbb", "1333");
            if (token == null) {
                System.out.println("Login failed.");
                return;
            }

            String department = "development";
            String filename = "concurrent_test.txt";

            // كتابة Thread: تحديث الملف
            Runnable writer = () -> {
                try {
                    String content = "Written by " + Thread.currentThread().getName();
                    boolean result = coordinator.updateFile(token, filename, content.getBytes());
                    System.out.println("[WRITE] " + Thread.currentThread().getName() + ": " + (result ? "Success" : "Fail"));
                } catch (Exception e) {
                    System.err.println("[WRITE ERROR] " + Thread.currentThread().getName() + ": " + e.getMessage());
                }
            };

            // قراءة Thread: قراءة الملف
            Runnable reader = () -> {
                try {
                    byte[] data = coordinator.fetchFile(token, department, filename);
                    if (data != null) {
                        System.out.println("[READ] " + Thread.currentThread().getName() + ": " + new String(data));
                    } else {
                        System.out.println("[READ] " + Thread.currentThread().getName() + ": File is null or not found.");
                    }
                } catch (Exception e) {
                    System.err.println("[READ ERROR] " + Thread.currentThread().getName() + ": " + e.getMessage());
                }
            };

            // تشغيل عدة Threads
            for (int i = 0; i < 3; i++) {

                new Thread(writer, "Writer-" + i).start();
                Thread.sleep(100);
                new Thread(reader, "Reader-" + i).start();
                Thread.sleep(100);

            }



        } catch (Exception e) {
            System.err.println("Error in test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package test;

import coordinator.CoordinatorService;
import node.NodeService; // تأكد من استيراد الخدمة الصحيحة
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ConcurrentWritersTest {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) registry.lookup("CoordinatorService");

            String token = coordinator.loginUser("bbb", "1333");
            if (token == null) {
                System.out.println("Login failed.");
                return;
            }

            String department = "development";
            String filename = "concurrent_test.txt";

            // تشغيل عدة Threads تحاول الكتابة في نفس الوقت
            for (int i = 0; i < 5; i++) {
                final int id = i;

                Thread.sleep(100);

                new Thread(() -> {
                    String content = "Written by Writer-" + id + "\n";
                    try {
                        boolean success = coordinator.updateFile(token, filename, content.getBytes());
                        System.out.println("[WRITE] Writer-" + id + ": " + (success ? "Success" : "Fail"));
                    } catch (Exception e) {
                        System.out.println("[ERROR] Writer-" + id + ": " + e.getMessage());
                    }
                }).start();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

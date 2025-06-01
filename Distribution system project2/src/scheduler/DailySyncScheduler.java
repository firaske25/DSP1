// File: scheduler/DailySyncScheduler.java
package scheduler;

import sync.FileSyncClient;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class DailySyncScheduler {
    public static void main(String[] args) {
        // جدول التنفيذ بعد 3 دقائق (3 * 60 * 1000 ملي ثانية)
        long delay = 15 * 1000;

        System.out.println("DailySyncScheduler is running... waiting for 30 seconds to start sync.");

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("🔁 Starting daily sync...");

                // لكل عقدة، نحدد مجلدها والعقد الأخرى
                new Thread(() -> {
                    new FileSyncClient("node1_data", Arrays.asList("localhost:6002", "localhost:6003")).syncAllDepartments();
                }).start();

                new Thread(() -> {
                    new FileSyncClient("node2_data", Arrays.asList("localhost:6001", "localhost:6003")).syncAllDepartments();
                }).start();

                new Thread(() -> {
                    new FileSyncClient("node3_data", Arrays.asList("localhost:6001", "localhost:6002")).syncAllDepartments();
                }).start();
            }
        }, delay);
    }
}

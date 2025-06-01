package clients;

import coordinator.CoordinatorService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.io.File;


public class MangerClient {
    public static void main(String[] args) {
        int c = 0;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) registry.lookup("CoordinatorService");

            Scanner scanner = new Scanner(System.in);
            System.out.println("=== Manager Login ===");
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

                // تحقق إذا كان users.txt فارغاً
                BufferedReader reader = new BufferedReader(new FileReader(new File("data/users.txt")));
                String l = reader.readLine();
                if(l == null) {

                    System.out.print("No users found. complete as a manager?");
                    String a = scanner.nextLine();

                    if (a.equalsIgnoreCase("y")) {
                        String response = coordinator.registerUser(username, password, "Manager", "null");
                        System.out.println("Registration response: " + response);
                    } else {
                        return;
                    }
                }

                     String token = coordinator.loginUser(username, password);
                    if (token == null) {
                        System.out.println("Login failed. Invalid credentials");
                        return;
                    }


            String role = coordinator.getUserRole(token);
            if (!role.equalsIgnoreCase("Manager")) {
                System.out.println("Access denied. Only managers can add employees.");
                return;
            }
            System.out.println("Login successful as Manager.");





            // إدخال بيانات موظفين جدد
            while (true) {
                System.out.println("\n=== Register New Employee ===");
                System.out.print("New Employee Username: ");
                String newUsername = scanner.nextLine();

                System.out.print("New Employee Password: ");
                String newPassword = scanner.nextLine();

                System.out.print("Department (development, QA, design): ");
                String dept = scanner.nextLine();

                String response = coordinator.registerUser(newUsername, newPassword, "Employee", dept);
                System.out.println("Response: " + response);

                System.out.print("Add another employee? (yes/no): ");
                String answer = scanner.nextLine();
                if (!answer.equalsIgnoreCase("yes")) {
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error connecting to Coordinator:");
            e.printStackTrace();
        }
    }
}

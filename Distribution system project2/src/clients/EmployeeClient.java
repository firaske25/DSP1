package clients;

import coordinator.CoordinatorService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;



public class EmployeeClient {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) registry.lookup("CoordinatorService");

            Scanner scanner = new Scanner(System.in);
            System.out.println("=== Employee Login ===");
            System.out.print("Username: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            String token = coordinator.loginUser(username, password);

            if(token != null){

                while (true) {
                    System.out.println("\n=== Employee Menu ===");
                    System.out.println("1. Upload File");
                    System.out.println("2. List My Files");
                    System.out.println("3. Update File");
                    System.out.println("4. Delete File");
                    System.out.println("5. Show File");
                    System.out.println("6. Exit");
                    System.out.print("Choose option: ");
                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1: {
                            System.out.print("Enter full path of file to upload: ");
                            String path = scanner.nextLine().replace("\"", "");
                            File file = new File(path);
                            if (!file.exists()) {
                                System.out.println("File does not exist.");
                                break;
                            }
                            byte[] data = Files.readAllBytes(file.toPath());
                            boolean success = coordinator.uploadFile(token, file.getName(), data);
                            System.out.println(success ? "File uploaded." : "Upload failed.");
                            break;
                        }

                        case 2: {
                            List<String> files = coordinator.listFiles(token);
                            System.out.println("Your files:");
                            for (String f : files) {
                                System.out.println("- " + f);
                            }
                            break;
                        }

                        case 3: {
                            List<String> files = coordinator.listFiles(token);
                            System.out.println("Your files:");
                            for (String f : files) {
                                System.out.println("- " + f);
                            }
                            System.out.print("Enter filename to update: ");
                            String filename = scanner.nextLine();

                            System.out.print("Enter new full path of replacement file: ");
                            String path = scanner.nextLine().replace("\"", "");
                            File newFile = new File(path);
                            if (!newFile.exists()) {
                                System.out.println("File does not exist.");
                                break;
                            }

                            byte[] newData = Files.readAllBytes(newFile.toPath());
                            boolean updated = coordinator.updateFile(token, filename, newData);
                            System.out.println(updated ? "File updated." : "Update failed.");
                            break;
                        }

                        case 4: {
                            List<String> files = coordinator.listFiles(token);
                            System.out.println("Your files:");
                            for (String f : files) {
                                System.out.println("- " + f);
                            }
                            System.out.print("Enter filename to delete: ");
                            String filename = scanner.nextLine();

                            boolean deleted = coordinator.deleteFile(token, filename);
                            System.out.println(deleted ? "File deleted." : "Delete failed.");
                            break;
                        }

                        case 5: {
                            System.out.print("Enter department of the file: ");
                            String dept = scanner.nextLine();
                            System.out.print("Enter file name: ");
                            String targetFile = scanner.nextLine();
                            byte[] content = coordinator.fetchFile(token, dept, targetFile);
                            if (content != null) {
                                File downloadDir = new File("downloads");
                                if (!downloadDir.exists()) {
                                    downloadDir.mkdirs();
                                }

                                Files.write(Paths.get("downloads/" + targetFile), content);
                                System.out.println("File downloaded to 'downloads/" + targetFile + "'");
                            } else {
                                System.out.println("File not found.");
                            }
                            break;
                        }

                        case 6:
                            System.out.println("Exiting...");
                            return;

                        default:
                            System.out.println("Invalid choice.");
                    }

                }

            }

            else if (token == null) {
                System.out.println("Login failed. Invalid credentials.");
            } else {
                System.out.println("Login successful!");
                System.out.println("Your token is: " + token);
            }


        } catch (Exception e) {
            System.out.println("Error connecting to Coordinator:");
            e.printStackTrace();
        }
    }
}







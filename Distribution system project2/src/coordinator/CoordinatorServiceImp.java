package coordinator;

import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import java.util.UUID;
import node.NodeService;
import sync.FileSyncClient;

public class CoordinatorServiceImp extends UnicastRemoteObject implements CoordinatorService {

    private static int ID = 1;
    private int currentSearchIndex = 0;

    private final File usersFile = new File("data/users.txt");
    private final File tokensFile = new File("data/tokens.txt");

    private final File idFile = new File("data/id.txt");

    private Map<String, User> tokenUserMap = new HashMap<>();
    private Map<Integer, User> usersById = new HashMap<>();
    private Map<Integer, NodeService> nodes = new HashMap<>();
    private Map<String,  List<Integer>> fileLocationMap = new HashMap<>();
    Map<String, User> activeTokens = new HashMap<>();
    private final List<Integer> nodeIds = new ArrayList<>();


    private Random random = new Random();

    // قائمة بمجلدات العقد مع المنافذ الخاصة بكل منها
    private final Map<String, Integer> nodeDataAndPorts = Map.of(
            "node1_data", 6001,
            "node2_data", 6002,
            "node3_data", 6003
    );


    public CoordinatorServiceImp() throws RemoteException {
        super();
        usersFile.getParentFile().mkdirs();
        try {
            loadUsers("data/users.txt");
            loadTokens("data/tokens.txt");
            loadId(); // تحميل ID من الملف
            usersFile.createNewFile();
            tokensFile.createNewFile();

            nodeIds.addAll(nodes.keySet());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadUsers(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) {
                    int userId = Integer.parseInt(parts[0]);
                    String username = parts[1];
                    String password = parts[2];
                    String role = parts[3];
                    String department = parts[4];

                    User user = new User(userId, username, password, role, department);
                    usersById.put(userId, user);
                }
            }
            System.out.println("Users loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }

    public void loadTokens(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 2) {
                    String token = parts[0];
                    int userId = Integer.parseInt(parts[1]);

                    User user = usersById.get(userId);
                    if (user != null) {

                        // activeTokens.put(token,user);
                        tokenUserMap.put(token, user);
                    }
                }
            }
            System.out.println("Tokens loaded successfully.");
        } catch (IOException e) {
            System.err.println("Failed to load tokens: " + e.getMessage());
        }
    }



    private void loadId() {
        try {
            if (idFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(idFile));
                String line = reader.readLine();
                if (line != null) {
                    ID = Integer.parseInt(line);
                }
                reader.close();
            } else {
                ID = 1; // القيمة الابتدائية في حال لم يوجد الملف
            }
        } catch (IOException | NumberFormatException e) {
            ID = 1; // fallback
            System.err.println("Error loading ID: " + e.getMessage());
        }
    }


    @Override
    public synchronized String registerUser(String username, String password, String role, String department) throws RemoteException {
        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(username + ";")) {
                    return "User already exists.";
                }
            }
        } catch (IOException e) {
            return "Error reading user file.";
        }

        try (FileWriter writer = new FileWriter(usersFile, true)) {
            writer.write(ID + ";" + username + ";" + password + ";" + role + ";" + department + "\n");

            User u = new User(ID, username, password, role, department);
            usersById.put(ID, u);

            String token = UUID.randomUUID().toString();
            activeTokens.put(token, u);
            saveToken(token, ID);
            ID++;
            saveId();

            System.out.println("User registered successfully.");
            return token;

        } catch (IOException e) {
            return "Error writing to user file.";
        }
    }

    @Override
    public String loginUser(String username, String password) throws RemoteException {
//        User user = usersById.get(username);

        for (Map.Entry<Integer, User> entry : usersById.entrySet()) {
            if (entry.getValue().getUsername().equals(username) && entry.getValue().getPassword().equals(password)) {
                String token = UUID.randomUUID().toString();

                activeTokens.put(token, entry.getValue());
                saveToken(token, entry.getKey());
                return token;
            }
        }
        return null;
    }

    private void saveToken(String token, int userID) {
        File tokenFile = new File("data/tokens.txt");
        List<String> lines = new ArrayList<>();
        boolean updated = false;

        try {
            // قراءة كل الأسطر من الملف
            if (tokenFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(tokenFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(";");
                        if (parts.length == 2 && parts[1].equals(String.valueOf(userID))) {
                            // استبدال السطر بالتوكن الجديد لنفس الـ userID
                            lines.add(token + ";" + userID);
                            updated = true;
                        } else {
                            lines.add(line); // نحتفظ بالسطر كما هو
                        }
                    }
                }
            }

            // إذا لم يتم تحديث أي سطر (userID جديد)، نضيف سطر جديد
            if (!updated) {
                lines.add(token + ";" + userID);
            }

            // الكتابة من جديد إلى الملف بالكامل
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tokenFile, false))) {
                for (String l : lines) {
                    writer.write(l);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to save token: " + e.getMessage());
        }
    }

    private void saveId() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(idFile))) {
            writer.write(String.valueOf(ID));
        } catch (IOException e) {
            System.err.println("Failed to save ID: " + e.getMessage());
        }
    }


    public String getUserRole(String token) throws RemoteException {
        User user = activeTokens.get(token);
        return (user != null) ? user.getRole() : null;
    }

    @Override
    public synchronized void registerNode(int nodeId, NodeService node) throws RemoteException {
        nodes.put(nodeId, node);
        if (!nodeIds.contains(nodeId)) {
            nodeIds.add(nodeId);
        }
        System.out.println("Node " + nodeId + " registered.");
    }

    private Map.Entry<Integer, NodeService> getRandomNode() {
        List<Map.Entry<Integer, NodeService>> nodeList = new ArrayList<>(nodes.entrySet());
        if (nodeList.isEmpty()) return null;
        return nodeList.get(new Random().nextInt(nodeList.size()));
    }

    @Override
    public boolean uploadFile(String token, String filename, byte[] data) throws RemoteException {
        User user = activeTokens.get(token);
        if (user == null) return false;

        if (nodes.size() < 2) {
            System.out.println("Not enough nodes for double replication.");
            return false;
        }

        // اختيار عقدتين عشوائيتين مختلفتين
        List<Map.Entry<Integer, NodeService>> nodeList = new ArrayList<>(nodes.entrySet());
        Collections.shuffle(nodeList);

        Map.Entry<Integer, NodeService> entry1 = nodeList.get(0);
        Map.Entry<Integer, NodeService> entry2 = nodeList.get(1);

        int nodeId1 = entry1.getKey();
        int nodeId2 = entry2.getKey();

        NodeService node1 = entry1.getValue();
        NodeService node2 = entry2.getValue();

        boolean success1 = false, success2 = false;

        try {
            success1 = node1.uploadFile(user.getDepartment(), filename, data);
        } catch (Exception e) {
            System.out.println("Node " + nodeId1 + " upload failed: " + e.getMessage());
        }

        try {
            success2 = node2.uploadFile(user.getDepartment(), filename, data);
        } catch (Exception e) {
            System.out.println("Node " + nodeId2 + " upload failed: " + e.getMessage());
        }

        if (success1 || success2) {
            String key = user.getDepartment() + "/" + filename;
            // هنا نحتاج أن نعدل نوع fileLocationMap ليكون Map<String, List<Integer>>
            fileLocationMap.put(key, Arrays.asList(nodeId1, nodeId2));
        }

        return success1 || success2;
    }


    @Override
    public boolean deleteFile(String token, String filename) throws RemoteException {
        User user = activeTokens.get(token);
        if (user == null) return false;

        String key = user.getDepartment() + "/" + filename;
        List<Integer> nodeIds = fileLocationMap.get(key);
        if (nodeIds == null || nodeIds.isEmpty()) return false;

        boolean allDeleted = true;
        for (Integer nodeId : nodeIds) {
            NodeService node = nodes.get(nodeId);
            if (node != null) {
                boolean deleted = node.deleteFile(user.getDepartment(), filename);
                if (!deleted) {
                    System.out.println("Failed to delete from node " + nodeId);
                    allDeleted = false;
                }
            } else {
                allDeleted = false;
            }
        }

        if (allDeleted) {
            fileLocationMap.remove(key);
        }

        return allDeleted;
    }



    @Override
    public boolean updateFile(String token, String filename, byte[] data) throws RemoteException {
        User user = activeTokens.get(token);
        if (user == null) return false;

        String key = user.getDepartment() + "/" + filename;
        List<Integer> nodeIds = fileLocationMap.get(key);
        if (nodeIds == null || nodeIds.isEmpty()) return false;

        boolean allUpdated = true;
        for (Integer nodeId : nodeIds) {
            NodeService node = nodes.get(nodeId);
            if (node != null) {
                boolean updated = node.updateFile(user.getDepartment(), filename, data);
                if (!updated) {
                    System.out.println("Failed to update file on node " + nodeId);
                    allUpdated = false;
                }
            } else {
                allUpdated = false;
            }
        }

        return allUpdated;
    }



    @Override
    public List<String> listFiles(String token) throws RemoteException {
        User user = activeTokens.get(token);
        if (user == null) return Collections.emptyList();

        Set<String> uniqueFiles = new HashSet<>();

        for (NodeService node : nodes.values()) {
            try {
                List<String> nodeFiles = node.listFiles(user.getDepartment());
                uniqueFiles.addAll(nodeFiles); // نتجنب التكرار إذا كان نفس الملف مكرر على أكثر من عقدة
            } catch (RemoteException e) {
                // يمكن تجاهل الخطأ من عقدة غير متوفرة
                System.err.println("Could not connect to a node while listing files.");
            }
        }

        return new ArrayList<>(uniqueFiles);
    }

    public void startDailySync() {
        new Thread(() -> {
            while (true) {
                try {
                    // إجراء المزامنة من كل عقدة إلى بقية العقد
                    for (Map.Entry<String, Integer> sourceEntry : nodeDataAndPorts.entrySet()) {
                        String sourceFolder = sourceEntry.getKey();
                        int sourcePort = sourceEntry.getValue();

                        List<String> targetAddresses = new ArrayList<>();
                        for (Map.Entry<String, Integer> targetEntry : nodeDataAndPorts.entrySet()) {
                            if (!targetEntry.getKey().equals(sourceFolder)) {
                                targetAddresses.add("localhost:" + targetEntry.getValue());
                            }
                        }

                        System.out.println("Starting sync from " + sourceFolder + " to " + targetAddresses);
                        FileSyncClient syncClient = new FileSyncClient(sourceFolder, targetAddresses);
                        syncClient.syncAllDepartments();
                    }

                    // ننتظر حتى الغد (للتجريب يمكن تقليلها إلى 30 ثانية مثلاً)
                    Thread.sleep(24 * 60 * 60 * 1000); // 24 ساعة

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public synchronized byte[] fetchFile(String token, String department, String filename) throws RemoteException {
        String key = department + "/" + filename;
        List<Integer> nodeIdsWithFile = fileLocationMap.get(key);

        if (nodeIdsWithFile == null || nodeIdsWithFile.isEmpty()) {
            return null; // لا توجد عقد تحتوي على الملف
        }

        int attempts = 0;
        int size = nodeIdsWithFile.size();

        while (attempts < size) {
            int index = currentSearchIndex % size;
            int nodeId = nodeIdsWithFile.get(index);
            currentSearchIndex = (currentSearchIndex + 1) % size;

            NodeService node = nodes.get(nodeId);
            if (node != null) {
                try {
                    byte[] data = node.getFileContent(department, filename);
                    if (data != null) return data;
                } catch (Exception e) {
                    // تجاهل الخطأ وحاول من نسخة أخرى
                }
            }

            attempts++;
        }

        return null; // لم يتم العثور على نسخة صالحة
    }
}

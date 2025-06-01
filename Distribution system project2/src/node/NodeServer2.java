package node;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import coordinator.CoordinatorService;
import sync.FileSyncClient;
import sync.FileSyncServer;

public class NodeServer2 {
    public static void main(String[] args) {

        String basePath = "node2_data";

        try {
            NodeServiceImpl nodeService = new NodeServiceImpl(basePath);
            Registry registry = LocateRegistry.createRegistry(1101);
            registry.rebind("NodeService2", nodeService);

            // بعد إنشاء NodeServiceImpl وربطه بـ RMI:
            Registry coordinatorRegistry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) coordinatorRegistry.lookup("CoordinatorService");
            coordinator.registerNode(2, nodeService);  // لكل عقدة رقم مختلف
            System.out.println("NodeService2 is running...");

            FileSyncServer syncServer = new FileSyncServer(basePath, 6002);
            new Thread(syncServer::start).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

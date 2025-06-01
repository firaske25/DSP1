package node;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import coordinator.CoordinatorService;
import sync.FileSyncClient;
import sync.FileSyncServer;


public class NodeServer3 {
    public static void main(String[] args) {

        String basePath = "node3_data";

        try {
            NodeServiceImpl nodeService = new NodeServiceImpl(basePath);
            Registry registry = LocateRegistry.createRegistry(1102);
            registry.rebind("NodeService3", nodeService);

            Registry coordinatorRegistry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) coordinatorRegistry.lookup("CoordinatorService");
            coordinator.registerNode(3, nodeService);  // لكل عقدة رقم مختلف
            System.out.println("NodeService3 is running...");

            FileSyncServer syncServer = new FileSyncServer(basePath, 6003);
            new Thread(syncServer::start).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

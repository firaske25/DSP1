package coordinator;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class CoordinatorServer {
    public static void main(String[] args) {
        try {
            CoordinatorServiceImp service = new CoordinatorServiceImp();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("CoordinatorService", service);
            System.out.println("Coordinator Service is running...");
//            service.startDailySync();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

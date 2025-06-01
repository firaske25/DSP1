package node;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import coordinator.CoordinatorService;
import sync.FileSyncServer;

public class NodeServer1 {
    public static void main(String[] args) {

        String basePath = "node1_data";

        try {
            // مسار ملفات هذه العقدة

            // إنشاء كائن الخدمة للعقدة
            NodeServiceImpl nodeService = new NodeServiceImpl(basePath);

            // تشغيل سجل RMI محلي على منفذ فريد لهذه العقدة (مثلاً 1100)
            Registry localRegistry = LocateRegistry.createRegistry(1100);
            localRegistry.rebind("NodeService1", nodeService);

            // الاتصال بسجل الـ Coordinator (الموجود على المنفذ 1099)
            Registry coordinatorRegistry = LocateRegistry.getRegistry("localhost", 1099);
            CoordinatorService coordinator = (CoordinatorService) coordinatorRegistry.lookup("CoordinatorService");

            // تسجيل هذه العقدة في الـ Coordinator
            coordinator.registerNode(1, nodeService);  // رقم العقدة = 1

            System.out.println("NodeService1 is running and registered with Coordinator.");

            FileSyncServer syncServer = new FileSyncServer(basePath, 6001);
            new Thread(syncServer::start).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

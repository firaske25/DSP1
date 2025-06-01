package coordinator;

import java.util.List;
import java.io.File;
import java.rmi.Remote;
import java.rmi.RemoteException;
import node.NodeService;

public interface CoordinatorService extends Remote {
    String registerUser(String username, String password, String role, String department) throws RemoteException;
    String loginUser(String username, String password) throws RemoteException;

    String getUserRole(String token) throws RemoteException; // يجب إضافتها هنا

    void    registerNode(int nodeId, NodeService node) throws RemoteException;

    boolean uploadFile(String token, String filename, byte[] data) throws RemoteException;

    boolean deleteFile(String token, String filename) throws RemoteException;

    boolean updateFile(String token, String filename, byte[] data) throws RemoteException;

    List<String> listFiles(String token) throws RemoteException;

    byte[] fetchFile(String token, String department, String filename) throws RemoteException;

}

package node;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NodeService extends Remote {
    boolean uploadFile(String department, String filename, byte[] fileData) throws RemoteException;
    boolean updateFile(String department, String filename, byte[] fileData) throws RemoteException;
    boolean deleteFile(String department, String filename) throws RemoteException;
    List<String> listFiles(String department) throws RemoteException;
    byte[] downloadFile(String department, String filename) throws RemoteException;

    byte[] getFileContent(String department, String fileName) throws RemoteException;
}

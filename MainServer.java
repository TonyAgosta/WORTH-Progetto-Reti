import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

public class MainServer {

    private static final int RMI_PORT = 6000;

    public static void main(String[] args) throws ExportException {

        try {
            RegisterCallback rc = new RegisterCallback();

            // esporta l'oggetto

            RegisterCallbackInterface rci = (RegisterCallbackInterface) UnicastRemoteObject.exportObject(rc, 39000);
            Server server = new Server(rc);
            RMI_Interface stub = (RMI_Interface) UnicastRemoteObject.exportObject(server, 39000);

            // crea il registro
            LocateRegistry.createRegistry(RMI_PORT);
            Registry register = LocateRegistry.getRegistry(RMI_PORT);
            LocateRegistry.createRegistry(3400);
            Registry reg = LocateRegistry.getRegistry(3400);

            // binding
            register.rebind("SERVER_RMI", stub);
            reg.rebind("CLIENT_REGISTER", rci);
            System.out.println("Server pronto");
            server.start();

        } catch (

        RemoteException e) {
            e.printStackTrace();
        }

    }
}

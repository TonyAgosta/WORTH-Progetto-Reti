
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;

public class MainClient {

    final static String DEFAULT_GROUP = "239.255.1.3";
    final static int DEFAULT_PORT = 60000;

    public static void main(String[] args) {
        try {
            Client client = new Client(DEFAULT_GROUP, DEFAULT_PORT);
            client.start();
        } catch (ConnectException e) {
            System.err.println("Ops! Il server non e` stato avviato");
        } catch (UnknownHostException e) {
            System.err.println("L'indirizzo immesso non e` valido");
        } catch (IllegalArgumentException e) {
            System.err.println("L'indirizzo immesso non e` un indirizzo  multicast");
        } catch (RemoteException e) {
            System.out.println("Ops, qualcosa e` andato storto!");
        }
        return;
    }
}

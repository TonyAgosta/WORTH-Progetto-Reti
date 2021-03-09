import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RegisterCallbackInterface extends Remote {
    // questa classe serve per inviare gli aggiornamenti sulle strutture dati agli
    // utenti che si sono registrati al servizio ma che non hanno ancora effettuato
    // il login per la prima volta

    // registrazione per la callback alla registrazione
    public void registerForCallbackAtRegister(NotifyInterface ClientInterface) throws RemoteException;

    // cancella registrazione per la callback al logout
    public void unregisterForCallbackAtLogout(NotifyInterface ClientInterface) throws RemoteException;

}
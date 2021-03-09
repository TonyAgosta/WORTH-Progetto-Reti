import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyInterface extends Remote {

    // metodo che notifica ai client se un utente ha effettuato il login o il
    // logout.
    public void notifyEvent(String username, String stato) throws RemoteException;

    // metodo che aggiorna la lista dei progetti degli utenti
    public void notifyEvent2(String username, String prog) throws RemoteException;

    // metodo che aggiorna le associazioni Progetti-Informazioni di connessione per
    // interagire con le chat
    public void notifyEvent3(String nomeprogetto, ConnectionINFO connectionInfo) throws RemoteException, IOException;

    // metodo che aggiorna la lista dei progetti degli utenti quando viene eliminato
    // un prozetto
    public void notifyEvent4(String nomeprogetto) throws RemoteException;

}

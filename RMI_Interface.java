import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface RMI_Interface extends Remote {
    // questa classe rappresenta il servizio di nitifiche a cui gli utenti si
    // registrano quando fanno il login

    // metodo usato per registrare un utente tramite il meccanismo delle RMI
    public String registerUser(String username, String password) throws RemoteException;

    // registrzione, al servizio di notifiche (Dopo il login),con RMI
    public void registerForCallback(NotifyInterface ClientInterface) throws RemoteException;

    // cancella la registrazione,al servizio di notifiche (Al Logout) con RMI
    public void unregisterForCallback(NotifyInterface ClientInterface) throws RemoteException;

}

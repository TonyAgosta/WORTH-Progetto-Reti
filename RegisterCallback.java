import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RegisterCallback extends RemoteServer implements RegisterCallbackInterface {

    static final long serialVersionUID = 1L;
    private List<NotifyInterface> clients;

    public RegisterCallback() throws RemoteException {
        clients = new ArrayList<NotifyInterface>();
    }

    public synchronized void registerForCallbackAtRegister(NotifyInterface ClientInterface) throws RemoteException {
        if (!clients.contains(ClientInterface)) {
            clients.add(ClientInterface);
            System.out.println("Nuovo client registrato");
        }
    }

    public synchronized void unregisterForCallbackAtLogout(NotifyInterface Client) throws RemoteException {
        if (clients.remove(Client)) {
            System.out.println("Client unregistered");
        }
    }

    public synchronized void doCallbacks2(ArrayList<Utente_Stato> val) throws RemoteException {
        System.out.println("Inizia la callback");
        Iterator i = clients.iterator();
        while (i.hasNext()) {
            NotifyInterface client = (NotifyInterface) i.next();
            for (int j = 0; j < val.size(); j++) {
                String username = val.get(j).getNomeUtente();
                int size = val.get(j).getListaProgetti().size();
                if (size > 0) {
                    String prog = val.get(j).getListaProgetti().get(size - 1);
                    try {
                        client.notifyEvent2(username, prog);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        System.out.println("Callbacks complete");
    }

    // per aggiornare la lista dei progetti
    public void update2(ArrayList<Utente_Stato> val) throws RemoteException {
        doCallbacks2(val);
    }

    public synchronized void doCallbacks3(HashMap<String, ConnectionINFO> ms) {
        System.out.println("Aggiorno la HashMap delle connessioni");
        Iterator i = clients.iterator();
        while (i.hasNext()) {
            NotifyInterface client = (NotifyInterface) i.next();
            for (String s : ms.keySet()) {
                try {
                    client.notifyEvent3(s, ms.get(s));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Aggiornamento HashMap completato");
    }

    public void update3(HashMap<String, ConnectionINFO> ms) {
        doCallbacks3(ms);

    }

    public synchronized void doCallbacks4(String nomeprogetto) {
        System.out.println("Notifico l'eliminazione di un progetto");
        Iterator i = clients.iterator();
        while (i.hasNext()) {
            NotifyInterface client = (NotifyInterface) i.next();
            try {
                client.notifyEvent4(nomeprogetto);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Notifica eliminazione proegetto terminata");
    }

    // cancella il progetto
    public void update4(String nomeprogetto) {
        doCallbacks4(nomeprogetto);
    }

}

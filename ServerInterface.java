import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerInterface {

    public String loginUser(String username, String password);

    /**
     * @Requires: username !=null && password !=null
     * @Throws : se username==null || password==null lancia l'eccezione
     *         NullPointerExcpetion
     * @Effects: Effettua il login di un utente gia` registrato per accedere al
     *           servizio.
     * @return:Il server risponde con un messaggio che indica l'esito
     *            dell'operazione
     */

    public String logoutUser(String username);

    /**
     * @requires: username!=null
     * @throws: se username==null lancia l'eccezione NullPointerExcpetion
     * @effects: effettua il logout dell'utente del servizio
     * @return:Il server risponde con un messaggio che indica l'esito
     *            dell'operazione
     */

    /*
     * operazione utilizzata per visualizzare la lista degli utenti registrati al
     * servizio e il loro stato(online/offline)
     */
    public String listUsers();

    /**
     * @return:Il server risponde con la lista degli utenti registrati al servizio e
     *            il loro stato(online/offline)
     */

    /* operazione utilizzata per visualizzare la lista degli utenti online */
    public String listOnlineUsers();

    /**
     * @return:Il server risponde con la lista degli utenti registrati al servizio e
     *            online
     */

    /* operazione per recuperare la lista dei progetti di cui l'utente e`membro */
    public String listProjects(String username);

    /**
     * @requires: username!=null
     * @Throws: se usernam==null lancia l'eccezione NullPointerException
     * @return:Il server risponde con la lista degli utenti registrati al servizio e
     *            il loro stato(online/offline)
     */

    /* operazione per richiedere la creazione di un nuovo progetto */
    public String createProject(String username, String nomeProgetto);

    /**
     * @requires: username!=null && nomeprogetto!=null
     * @throws: se username==null || nomeprogetto==null lancia l'eccezione
     *             NullPointerException
     * @modifies: this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per aggiungere "username" al progetto "progetto" da parte
     * dell'utente "nomeutente
     */
    public String addMember(String nomeutente, String progetto, String username);

    /**
     * @requires:nomeutente!=null && progetto!=null && username!=null
     * @throws: se nomeutente==null || progetto==null || username==nul lancia
     *             l'eccezione NullPOinterException
     * @modifies: this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per recuperare la lista dei membri del progetto "nomeprogetto" da
     * parte dell'utente "username"
     */
    public String showMembers(String nomeprogetto, String username);

    /**
     * @requires: nomeprogetto!=null && username !=null
     * @Throws: se nomeprogetto==null || username==null lancia l'eccezione
     *          NullPointerException
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per recuperare la lista di card associate al
     * progetto"nomeprogetto" da parte dell'utente "username"
     */
    public String showCards(String nomeprogetto, String username);

    /**
     * @requires: nomeprogetto!=null && username !=null
     * @Throws: se nomeprogetto==null || username==null lancia l'eccezione
     *          NullPointerException
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per recuperare le informazioni(nome,descrizione testuale,lista in
     * cui si trova in quel momento) della carta "nomecarta" del
     * progetto"nomeprogetto" da parte dell'utente "username"
     */
    public String showCard(String nomeprogetto, String nomecarta, String username);

    /**
     * @requires: nomeprogetto!=null && username !=null && username!=null
     * @Throws: se nomeprogetto==null || username==null || nomecarta==null lancia
     *          l'eccezione NullPointerException
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per richiedere l'aggiunta della carta"nomecarta" al progetto
     * "nomeprogetto" da parte dell'utente "username".La carta viene aggiunta alla
     * lista "todo" ed accompagata da una descrizione data da "descrizione"
     */
    public String addCard(String nomeprogetto, String nomecarta, String descrizione, String username);

    /**
     * @requires: nomeprogetto!=null && nomecarta!=null && descrizione!=null &&
     *            username!=null
     * @throws: se nomeprogetto==null || nomecarta==null||descrizione==null||
     *             username==null lancia l'eccezione NullPointerExcpetion
     * @modifies: this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per richiedere lo spostamento della carta "nomecarta" appartenente
     * al progetto "nomeprogetto" dalla lista di partenza "listainiz" alla lista di
     * destinzazione "listadest" da parte dell'utente "username"
     */
    public String moveCard(String username, String nomeprogetto, String nomecarta, String listainiz, String listadest);

    /**
     * @requires: nomeprogetto!=null && nomecarta!=null && listainiz!=null &&
     *            listadest!=null
     * @throws: se nomeprogetto==null || nomecarta==null||listainiz==null||
     *             listadest==null lancia l'eccezione NullPointerExcpetion
     * @modifies: this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per richiedere la cronologia della carta "nomecarta" appartenente
     * al progetto "nomeprogetto" da parte dell'utente "nomeutente"
     */
    public String getCardHistory(String nomeprogetto, String nomeCarta, String nomeutente);

    /**
     * @requires: nomeprogetto!=null && nomecarta!=null && nomeutente!=null
     * @throws: se nomeprogetto==null || nomecarta==null|| nomeutente==null lancia
     *             l'eccezione NullPointerExcpetion
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per richiedere l'eliminazione del progetto "nomeprogetto" da parte
     * dell'utente "username".NB:il progetto puo` essere eliminato solamente se
     * tutte le sue card si trovano nella lista "done"
     */
    public String cancelProject(String nomeprogetto, String username);

    /**
     * @requires: nomeprogetto!=null && username!=null
     * @throws: se nomeprogetto==null|| username==null lancia l'eccezione
     *             NullPointerExcpetion
     * @modifies: this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per inviare une messaggio "messaggio" nella chat del progetto
     * "progetto" da parte dell'utente "username"
     */
    public String sendMessage(String progetto, String username, String messaggio);

    /**
     * @requires : progetto!=null !=null && username!=null &&messaggio!=null
     * @throws : se nomeprogetto==null || username==null || messaggio==null lancia
     *           l'eccezione NullPointerExcpetion
     * @modifies : this.
     * @return: il server risponde con l'esito dell'operazione
     */

    /*
     * operazione per leggere la chat del progetto "progetto" da parte dell'utente
     * "username"
     */
    public String readMessage(String progetto, String username);

    /**
     * @requires : progetto!=null && username!=null
     * @throws : se nomeprogetto==null || nomecarta==null||descrizione==null||
     *           username==null lancia l'eccezione NullPointerExcpetion
     * @return: il server risponde con l'esito dell'operazione
     */

    /* Registra un utente al servizio di norifiche al momento del login */
    public void registerForCallback(NotifyInterface ClientInterface) throws RemoteException;

    /**
     * @requires : ClientInterface deve essere un RemoteObject
     * @throws : se ClientInterface non e` un RemoteObject lancia l'eccezione
     *           RemoteException
     * @modifies : this.
     */

    public void unregisterForCallback(NotifyInterface ClientInterface) throws RemoteException;

    /**
     * @requires : ClientInterface deve essere un RemoteObject
     * @throws : se ClientInterface non e` un RemoteObject lancia l'eccezione
     *           RemoteException
     * @modifies : this.
     */

    /*
     * meotodo utilizzare per mandare aggiornamenti ,riguardo lo status degli
     * utenti, ai client registrati al servizio di notifica. PS: e` un metodo di
     * supporto per "update"
     */
    public void doCallbacks(ArrayList<Utente_Stato> val) throws RemoteException;

    /**
     * @requires: val!=null
     * @throws : se val==null lancia l'eccezione NullPointerException, lancia
     *           l'eccezione RemoteException se ci sono problemi con la lista di
     *           tipo NotifyInterface con cui lavora
     */

    /*
     * metodo utilizzato per notificare l'aggiornamento dello status di un utente.
     * Utilizza il metodo doCallbacks per mandare la notifica a tutti i client
     * registrati al servizio di notifica
     */
    public void update(ArrayList<Utente_Stato> val) throws RemoteException;

    /**
     * @requires: val!=null
     * @throws : se val==null lancia l'eccezione NullPointerException, lancia
     *           l'eccezione RemoteException se ci sono problemi con la lista di
     *           tipo NotifyInterface con cui lavora
     */

    /*
     * meotodo utilizzare per mandare aggiornamenti ,riguardo la lista dei progetti,
     * ai client registrati al servizio di notifica. PS: e` un metodo di supporto
     * per "updateProjectList"
     */
    public void doCallbacksProjectList(ArrayList<Utente_Stato> val) throws RemoteException;

    /**
     * @requires: val!=null
     * @throws : se val==null lancia l'eccezione NullPointerException, lancia
     *           l'eccezione RemoteException se ci sono problemi con la lista di
     *           tipo NotifyInterface con cui lavora
     */

    /*
     * metodo utilizzato per notificare l'aggiornamento della lista dei progetti.
     * Utilizza il metodo doCallbacks per mandare la notifica a tutti i client
     * registrati al servizio di notifica
     */
    public void updateProjectList(ArrayList<Utente_Stato> val) throws RemoteException;

    /**
     * @requires: val!=null
     * @throws : se val==null lancia l'eccezione NullPointerException, lancia
     *           l'eccezione RemoteException se ci sono problemi con la lista di
     *           tipo NotifyInterface con cui lavora
     */

    public void doCallbacksHashMap(HashMap<String, ConnectionINFO> ms);

    /*
     * metodo utilizzato per notificare l'aggiornamento della hashmap che contiene
     * le informazioni di connessioni per le chat dei progetti. Utilizza il metodo
     * doCallbacks per mandare la notifica a tutti i client registrati al servizio
     * di notifica
     */
    public void updateHashMap(HashMap<String, ConnectionINFO> ms);
    /**
     * @requires: ms!=null
     * @throws : se ms==null lancia l'eccezione NullPointerException, lancia
     *           l'eccezione RemoteException se ci sono problemi con la lista di
     *           tipo NotifyInterface con cui lavora
     */

}

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Server extends RemoteObject implements ServerInterface, RMI_Interface {

    private static final long serialVersionUID = 1L;
    public ArrayList<Utenti> utenti;
    public ArrayList<Progetto> progetti;
    public ArrayList<Utente_Stato> utenti_stato;
    private ObjectMapper objectMapper; // mapper per json
    public final int TCP_PORT = 60000; // TCP_PORT
    private List<NotifyInterface> clients; // Lista di client per callback
    private HashMap<String, ConnectionINFO> hasmp; // hashmap che associa a un progetto le inforamazioni connessioni per
                                                   // le chat
    private HashMap<String, ArrayList<String>> Progetti_Carte; // hashmap che associa a un progetto le relative carte
                                                               // (per il backup)
    private RegisterCallback rc; // gestisce le notifiche degli utenti che hannp fatto solo la registrazione ma
                                 // non il login

    // Files per Backup
    private File worthBackup; // Directory per il Backup
    private File restoreUtenti;
    private File restoreProgetti;
    private File restoreConnectionInfo;
    private File restoreCartellaProgetti;

    public Server(RegisterCallback rc) {
        super();
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        clients = new ArrayList<NotifyInterface>();
        utenti_stato = new ArrayList<Utente_Stato>();
        Progetti_Carte = new HashMap<>();
        this.rc = rc;
        restoreBackup();

    }

    public void start() {
        ServerSocketChannel ssc;
        Selector selector;
        ServerSocket socket;
        try {
            // Apertura e configurazione della socket per comunicare con il client
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(TCP_PORT));
            ssc.configureBlocking(false);

            selector = Selector.open();
            ssc.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Server WORTH Online!");
        while (true) {
            try {
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            Set<SelectionKey> readyKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                try {
                    if (key.isAcceptable()) { // "Catturo" le richieste di connessione
                        System.out.println("Server connesso con il Client");
                        ServerSocketChannel server = (ServerSocketChannel) key.channel();
                        SocketChannel client = server.accept();
                        client.configureBlocking(false);
                        SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);

                    } else if (key.isWritable()) {// "Catturo" di lettura
                        SocketChannel client = (SocketChannel) key.channel();
                        String output = (String) key.attachment();
                        if (output != null) {
                            ByteBuffer bufstring = ByteBuffer.wrap(output.getBytes());
                            int numbyte = client.write(bufstring);
                            // se la write restituisce -1 cancello la chiave e chiudo il canale associato
                            if (numbyte == -1) {
                                key.cancel();
                                key.channel().close();
                            } else if (bufstring.hasRemaining()) {
                                bufstring.flip();
                                String messaggio = StandardCharsets.UTF_8.decode(bufstring).toString();
                                key.attach(messaggio);

                            } else {
                                key.attach(null);
                                key.interestOps(SelectionKey.OP_READ);
                            }

                        }

                    } else if (key.isReadable()) { // "Catturo" le richieste di lettura
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer messclient = ByteBuffer.allocate(1024); // buffer per leggere le operazioni da
                                                                           // svolegere
                        messclient.clear();
                        int numbyte = client.read(messclient);
                        if (numbyte == -1) { // se la read restituisce -1 cancello la chiave e chiudo il canale
                                             // associato
                            key.cancel();
                            key.channel().close();
                        } else {
                            String risposta = new String(messclient.array()).trim();
                            String[] split = risposta.split(" ");
                            String operazione = split[0];
                            String b = " ";
                            if (operazione.equals("Login")) {
                                System.out.println("Richiesta di Login");
                                String username = split[1];
                                String password = split[2];
                                b = loginUser(username, password);
                                if (b.equals("Login effettuato")) {
                                    update(utenti_stato);// notifico ai client che un nuovo utente ha fatto il login
                                    updateProjectList(utenti_stato);// mando ai client,che hanno fatto il login che la
                                                                    // lista dei progetti
                                                                    // aggiornata
                                    updateHashMap(hasmp);// mando ai client la hashmap ,delle informazioni di
                                                         // connessione, aggiornata
                                }
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("Logout")) {
                                System.out.println("Richiesta di Logout");
                                String username = split[1];
                                b = logoutUser(username);
                                if (b.equals("Logout effettuato")) {
                                    update(utenti_stato);// notifico ai client che un utente ha fatto il logout
                                }
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("ListaProgetti")) {
                                System.out.println("Richiesta la lista dei progetti");
                                String username = split[1];
                                b = listProjects(username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("CreaProgetto")) {
                                System.out.println("Richiesta la creazione di un progetto");
                                String username = split[1];
                                String nomeprogetto = split[2];
                                String b1 = createProject(username, nomeprogetto);
                                updateProjectList(utenti_stato);// aggiorno la lista dei progetti degli utenti
                                updateHashMap(hasmp);// mando ai client la hashmap ,delle informazioni di
                                                     // connessione, aggiornata
                                key.attach(b1);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("AggiungiMembro")) {
                                System.out.println("Aggiunta di un nuovo membro");
                                String nomeutente = split[1]; // utente da aggiungere al progetto
                                String nomeprogetto = split[2];
                                String username = split[3];
                                b = addMember(nomeutente, nomeprogetto, username);
                                rc.update2(utenti_stato); // aggiorna la lista dei progetti degli utenti registrati al
                                                          // servizio ma chhe non hanno fatto il Login
                                rc.update3(hasmp);// mando, agli utenti registrati ma offline, la hashmap delle
                                                  // informazioni di connessione, aggiornata
                                updateHashMap(hasmp);// mando ai client la hashmap ,delle informazioni di
                                                     // connessione, aggiornata
                                updateProjectList(utenti_stato);// mando ai client,che hanno fatto il login che la
                                                                // lista dei progetti aggiornata
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("MostraMembri")) {
                                System.out.println("Richiesta la lista dei membri di un progetto");
                                String nomeprogetto = split[1];
                                String username = split[2];
                                b = showMembers(nomeprogetto, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("MostraCarte")) {
                                System.out.println("Richiesta la lista delle card di un progetto");
                                String nomeprogetto = split[1];
                                String username = split[2];
                                b = showCards(nomeprogetto, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);

                            }
                            if (operazione.equals("MostraCarta")) {
                                System.out.println("Richiesta di una carta");
                                String nomeprogetto = split[1];
                                String nomecarta = split[2];
                                String username = split[3];
                                b = showCard(nomeprogetto, nomecarta, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);

                            }
                            if (operazione.equals("AggiungiCarta")) {
                                System.out.println("Aggiunta di una carta");
                                String nomeProgetto = split[1];
                                String nomecarta = split[2];
                                String username = split[3];
                                String descrizione = "";
                                for (int i = 4; i < split.length; i++) {// leggo la descrizione della carta
                                    descrizione = descrizione + " " + split[i];
                                }
                                b = addCard(nomeProgetto, nomecarta, descrizione, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("SpostaCarta")) {
                                System.out.println("Richiesta di spostare una carta");
                                String nomecarta = split[1];
                                String nomeProgetto = split[2];
                                String listainiz = split[3];
                                String listades = split[4];
                                String username = split[5];
                                b = moveCard(username, nomeProgetto, nomecarta, listainiz, listades);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("CronologiaCarta")) {
                                System.out.println("Richiesta la cronologia di una carta");
                                String nomeProgetto = split[1];
                                String nomecarta = split[2];
                                String username = split[3];
                                b = getCardHistory(nomeProgetto, nomecarta, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("CancellaProgetto")) {
                                System.out.println("Richiesta l'eliminazione di un progetto");
                                String nomeprogetto = split[1];
                                String username = split[2];
                                b = cancelProject(nomeprogetto, username);
                                updateProjectList(utenti_stato);// mando agli utenti,che hanno fatto il login, la lista
                                                                // dei progetti aggiornata
                                updateHashMap(hasmp);// mando agliutenti,che hanno fatto il login, la hashmap ,delle
                                                     // informazioni di
                                                     // connessione, aggiornata
                                rc.update2(utenti_stato); // aggiorna la lista dei progetti degli utenti registrati al
                                                          // servizio ma che non hanno fatto il Login
                                rc.update4(nomeprogetto); // rimuovo il progetto dalla lista dei progetti degli utenti
                                                          // registrati al servizio ma chhe non hanno fatto il Login
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("InviaMessaggio")) {
                                System.out.println("Richiesto l'invio di un messaggio");
                                String nomeProgetto = split[1];
                                String username = split[2];
                                String messaggio = split[3];
                                b = sendMessage(nomeProgetto, username, messaggio);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                            if (operazione.equals("LeggiMessaggi")) {
                                System.out.println("Richiesta la lettura dei messaggi");
                                String nomeProgetto = split[1];
                                String username = split[2];
                                b = readMessage(nomeProgetto, username);
                                key.attach(b);
                                key.interestOps(SelectionKey.OP_WRITE);
                            }
                        }

                    }

                } catch (IOException e) {
                    key.cancel();
                    try {
                        key.channel().close();
                    } catch (IOException cex) {
                    }
                }

            }

        }
        try {
            ssc.socket().close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Ripristino lo stato del sistema
    public void restoreBackup() {
        worthBackup = new File("./WorthBackup");
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        restoreUtenti = new File("./WorthBackup/Utenti.json");
        restoreProgetti = new File("./WorthBackup/Progetti.json");
        restoreConnectionInfo = new File("./WorthBackup/ConnectionInfo.json");
        restoreCartellaProgetti = new File("./WorthBackup/Progetti");
        if (!worthBackup.exists()) { // se non esiste la directory per il backup la creo
            worthBackup.mkdir();
        }
        try {

            if (!restoreUtenti.exists()) {// se non esiste il file per il backup degli utenti la creo
                restoreUtenti.createNewFile();
                utenti = new ArrayList<Utenti>();
                objectMapper.writeValue(restoreUtenti, utenti);
            } else {// ripristino la lista degli utenti e il loro stato
                utenti = objectMapper.readValue(restoreUtenti, new TypeReference<ArrayList<Utenti>>() {
                });
                for (int i = 0; i < utenti.size(); i++) { // riprisitino la lista degli utenti da utilizzare per gli
                                                          // aggiornamenti ai client
                    Utente_Stato newUtente_stato = new Utente_Stato();
                    newUtente_stato.setNomeUtente(utenti.get(i).getNickName());
                    newUtente_stato.setStato("offline");
                    utenti_stato.add(newUtente_stato);
                }
            }

            if (!restoreProgetti.exists()) {// se non esiste il file per il backup dei la creo
                restoreProgetti.createNewFile();
                progetti = new ArrayList<Progetto>();
                objectMapper.writeValue(restoreProgetti, progetti);
            } else {// ripristino la lista dei progetti
                progetti = objectMapper.readValue(restoreProgetti, new TypeReference<ArrayList<Progetto>>() {
                });
                for (int j = 0; j < utenti_stato.size(); j++) {
                    for (int i = 0; i < progetti.size(); i++) {// ripristino la lista dei progetti da utilizzare per gli
                                                               // aggiornamenti ai client
                        if (progetti.get(i).getMemebers().contains(utenti_stato.get(j).getNomeUtente())) {
                            utenti_stato.get(j).getListaProgetti().add(progetti.get(i).getProjectName());
                        }
                    }
                }
            }
            if (!restoreCartellaProgetti.exists()) {// se non esiste la directory per il backup dei progetti la creo
                restoreCartellaProgetti.mkdir();
            } else// ripristino la directory contenente le directory che rappresentano i progetti
                  // e che contengono le carte del progetto specifico
                for (File directory : restoreCartellaProgetti.listFiles()) {
                    String prog = directory.getName();
                    Progetti_Carte.put(prog, new ArrayList<String>());
                    for (File namecards : directory.listFiles())
                        Progetti_Carte.get(prog).add(namecards.getName());// ripristino la hashmap che tiene le
                                                                          // associazioni progetti-card
                }

            if (!restoreConnectionInfo.exists()) {// se non esiste il file per il ripristino delle informazioni di
                                                  // connessioni lo creo
                restoreConnectionInfo.createNewFile();
                hasmp = new HashMap<>();
                objectMapper.writeValue(restoreConnectionInfo, hasmp);
            } else {// ripristino la hashmpa che mantiene le informazioni di connessione per le chat
                    // dei sinogli progetti
                hasmp = objectMapper.readValue(restoreConnectionInfo,
                        new TypeReference<HashMap<String, ConnectionINFO>>() {
                        });
                for (String s : hasmp.keySet()) {
                    int port = hasmp.get(s).getPort();
                    MulticastSocket ms = new MulticastSocket(port);
                    hasmp.get(s).setMulticastSocket(ms);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // utilizzato tramite RMI
    public String registerUser(String username, String password) {
        System.out.println("Un utente ha richiesto la registrazione");
        if (username == null)
            throw new IllegalArgumentException("Nome non valido");
        if (password == null)
            throw new IllegalArgumentException("Password non valida");
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                return "Utente gia` registrato";
            }
        }

        // creo il nuovo utente da aggiungere
        Utenti newUtente = new Utenti();
        newUtente.setNickName(username);
        newUtente.setPassword(password);
        newUtente.setStatus("offline");
        utenti.add(newUtente);
        Utente_Stato newutente_stato = new Utente_Stato();
        newutente_stato.setNomeUtente(username);
        newutente_stato.setStato("offline");
        utenti_stato.add(newutente_stato);
        try {
            update(utenti_stato);// aggiorno la lista di utenti-stato
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        try {
            objectMapper.writeValue(restoreUtenti, utenti);// scrivo nel file per il backup
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Utente registrato con successo";
    }

    public String loginUser(String username, String password) {
        boolean ok = false;
        if (username == null)
            throw new IllegalArgumentException("Nome non valido");
        if (password == null)
            throw new IllegalArgumentException("Password non valida");
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {// cerco l'utente nella lista degli utenti registrati al
                                                               // servizio
                ok = true;
                if (utenti.get(i).getPassword().equals(password)) {
                    if (utenti.get(i).getStatus() != "online") {
                        utenti.get(i).setStatus("online");
                        System.out.println("Login effettuato");
                        return "Login effettuato";
                    } else
                        return "Utente gia` online!";
                } else
                    return "Password errata!";
            }
        }
        if (ok) {// aggiorno anche lo stato dell'utente nella struttura dati da mandare al client
                 // con RMI
            for (int j = 0; j < utenti_stato.size(); j++) {
                if (utenti_stato.get(j).getNomeUtente().equals(username)) {
                    utenti_stato.get(j).setStato("online");
                }
            }
        }

        System.out.println("Login non effettuato");
        return "Login non riuscito";
    }

    public String logoutUser(String username) {
        boolean ok = false;
        String res = "Logout non effettuato";
        if (username == null)
            throw new IllegalArgumentException("Nome non valido");

        for (int i = 0; i < utenti.size(); i++) {// cerco l'utente nella lista degli utenti registrati al
                                                 // servizio
            if (utenti.get(i).getNickName().equals(username)) {
                if (utenti.get(i).getStatus().equals("online")) {
                    utenti.get(i).setStatus("offline");
                    res = "Logout effettuato";
                    System.out.println("Logout effettuato");
                } else
                    res = "Utente non online, effettuare prima l'accesso";
            }
        }
        if (ok) {// aggiorno anche lo stato dell'utente nella struttura dati da mandare al client
                 // con RMI
            for (int j = 0; j < utenti_stato.size(); j++) {
                if (utenti_stato.get(j).getNomeUtente().equals(username)) {
                    utenti_stato.get(j).setStato("offline");
                }
            }
        }
        System.out.println(res);
        return res;
    }

    public String listUsers() {
        ArrayList<String> utenti_stato = new ArrayList<String>();
        String nomeutente;
        String stato;
        for (int i = 0; i < utenti.size(); i++) {
            nomeutente = utenti.get(i).getNickName();
            stato = utenti.get(i).getStatus();
            utenti_stato.add(nomeutente + stato);
        }
        return utenti_stato.toString();
    }

    public String listOnlineUsers() {
        String res = "Nessun utente online";
        ArrayList<String> utenti_online = new ArrayList<String>();
        String nomeutente;
        for (int i = 0; i < utenti.size(); i++) {
            nomeutente = utenti.get(i).getNickName();
            if (utenti.get(i).getStatus().equals("online")) {
                utenti_online.add(nomeutente);
            }
        }
        if (utenti_online.size() > 0)// se ==0 nessun utente e` online
            res = utenti_online.toString();
        return res;
    }

    public String listProjects(String username) {
        if (username == null)
            throw new NullPointerException();
        String res = new String();
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username))
                res = utenti.get(i).getListaProgetti().toString();
        }
        return res;
    }

    public String createProject(String username, String nomeProgetto) {
        boolean ok = false;
        if (username == null || nomeProgetto == null)
            throw new NullPointerException();
        String b = "progetto non creato";
        for (int i = 0; i < utenti.size(); i++) {// cerco l'utente nella lista degli utenti registrati al servizio
            if (utenti.get(i).getNickName().equals(username)) {
                ok = true;
                Random r = new Random();
                int pp = r.nextInt(239);// numero random per la prima parte dell'indirizzo ip
                while (pp < 224)// rispetto l'intervallo da 224 a 239 per gli indirizzi multicast
                    pp = r.nextInt(239);
                String ip = pp + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);// compongo
                                                                                                    // l'indirizzo ip
                int project_port = r.nextInt(65535);// numero di porta random

                // il numero di porta deve essere > 1026 e != da quello utilizzato per la
                // comunicazione TCP
                while ((project_port <= 1025) || (project_port == 60000))
                    project_port = r.nextInt(65535);
                try {
                    // creo le informazioni per il multicastsocket da usare nella chat associata al
                    // progetto
                    MulticastSocket ms = new MulticastSocket(project_port);
                    Progetto progetto = new Progetto(); // creo un nuovo progetto
                    progetto.setIpAddress(ip);
                    progetto.setProjectName(nomeProgetto);
                    progetto.setProjectPort(project_port);
                    progetto.addMembers(username);
                    progetto.setMembers(progetto.getMemebers());
                    progetti.add(progetto); // aggiungo il progetto creato alla lista dei progetti
                    Progetti_Carte.put(nomeProgetto, new ArrayList<String>());// creo l'associazione progetto-carte del
                                                                              // progetto
                    utenti.get(i).addProgetto(nomeProgetto);// aggiorno la lista dei progetti dell'utente che ne ha
                                                            // chiesto la creazione
                    utenti.get(i).setListaProgetti(utenti.get(i).getListaProgetti());
                    ConnectionINFO connectionInfo = new ConnectionINFO();
                    connectionInfo.setMulticastSocket(ms);
                    connectionInfo.setNomeProgetto(nomeProgetto);
                    connectionInfo.setAddress(ip);
                    connectionInfo.setPort(project_port);
                    hasmp.put(nomeProgetto, connectionInfo);// creo l'associazione progetto-informazioni per la
                                                            // connessione delle chat
                    b = "Progetto creato";
                    // aggiornamento dei file per il backup
                    objectMapper.writeValue(restoreUtenti, utenti);
                    objectMapper.writeValue(restoreProgetti, progetti);
                    objectMapper.writeValue(restoreConnectionInfo, hasmp);
                    // creo la nuova directory per il nuovo progetto
                    File newProgetto = new File("./WorthBackup/Progetti/" + nomeProgetto);
                    newProgetto.mkdir();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (ok) {// aggiorno anche lo stato dell'utente nella struttura dati da mandare al client
                 // con RMI
            for (int j = 0; j < utenti_stato.size(); j++) {
                if (utenti_stato.get(j).getNomeUtente().equals(username)) {
                    utenti_stato.get(j).aggironalistaprogetti(nomeProgetto);
                }
            }
        }
        return b;
    }

    public String addMember(String nomeutente, String progetto, String username) {
        boolean b = false;
        boolean abilitato = false;
        boolean registrato = false;
        int j = -1;
        int x = -1;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                b = true; // l'utente che richiede di aggiugere il membro e` registrato al servizio
                j = i; // inidice dell'utente che vuole aggiungere un nuovo membro all'interno dell
                       // array degli utenti registrati al
                       // servizio
            }
            if (utenti.get(i).getNickName().equals(nomeutente)) {
                registrato = true; // il membro da aggiungere e` registrato al servizio
                x = i;
            }
        }
        if (b) {
            ArrayList<String> listprog;
            listprog = utenti.get(j).getListaProgetti();
            for (int i = 0; i < listprog.size(); i++) {// cerco il progetto nella lista dei progetti dell'utente
                if (listprog.get(i).equals(progetto)) {
                    abilitato = true;
                }
            }
            if (abilitato) {// l'utente e` membro del progetto in questione
                if (registrato) {
                    utenti.get(x).getListaProgetti().add(progetto);// aggiorno la lista dei progetti del membro aggiunto
                    for (int y = 0; y < utenti_stato.size(); y++) {
                        if (utenti_stato.get(y).getNomeUtente().equals(nomeutente)) {
                            utenti_stato.get(y).aggironalistaprogetti(progetto);// aggiorno la struttura dati utilizzata
                                                                                // per gli aggiornamenti tramite RMI del
                                                                                // membro aggiunto
                        }
                    }
                } else {
                    return "L'utente non puo` essere aggiunto perche` non e` registrato al servizio";
                }
                // aggiorno la lista dei membri del progetto
                for (int i = 0; i < progetti.size(); i++) {
                    if (progetti.get(i).getProjectName().equals(progetto)) {
                        progetti.get(i).addMembers(nomeutente);
                        progetti.get(i).setMembers(progetti.get(i).getMemebers());
                    }
                }
                // aggiorno i file per il backup
                try {
                    objectMapper.writeValue(restoreUtenti, utenti);
                    objectMapper.writeValue(restoreProgetti, progetti);
                    objectMapper.writeValue(restoreConnectionInfo, hasmp);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                return "Non sei membro del progetto";
            }
        } else {
            return "Utente non registrato al servizio";
        }
        return "Membro aggiunto con successo!";
    }

    public String showMembers(String nomeprogetto, String username) {
        if (nomeprogetto == null || username == null)
            throw new NullPointerException();
        boolean b = false;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                b = true; // l'utente che richiede la lista dei membri del progetto e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {
                if (progetti.get(i).getMemebers().contains(username)) {
                    return progetti.get(i).getMemebers().toString();
                }
            }
        }
        return "La lista dei membri non puo` essere recuperata";
    }

    public String showCards(String nomeprogetto, String username) {
        if (nomeprogetto == null || username == null)
            throw new NullPointerException();
        boolean b = false;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {

                b = true; // l'utente che richiede la lista dei membri del progetto e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {
                if (progetti.get(i).getMemebers().contains(username)) {
                    return progetti.get(i).getListCards().toString();
                }
            }
        }
        return "Non e` possibile recuperare la lista delle cards";
    }

    public String showCard(String nomeprogetto, String nomecarta, String username) {
        if (nomeprogetto == null || username == null || nomecarta == null)
            throw new NullPointerException();
        boolean b = false;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {

                b = true; // l'utente che richiede la lista dei membri del progetto e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {
                if (progetti.get(i).getMemebers().contains(username)) {
                    if (progetti.get(i).getCard(nomecarta) != null)
                        return progetti.get(i).getCard(nomecarta);
                }
            }

        }
        return "La carta non puo` essere recuperata";
    }

    public String addCard(String nomeprogetto, String nomecarta, String descrizione, String username) {
        if (nomeprogetto == null || nomecarta == null || descrizione == null || username == null)
            throw new NullPointerException();
        boolean b = false;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                b = true; // l'utente che richiede di aggiungere la carta e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {// cerco il progetto a cui aggiungere la carta
                if (progetti.get(i).getMemebers().contains(username)) {// se l'utente fa parte del progetto
                    if (progetti.get(i).getCard(nomecarta) == null) {// se ==null la carta non esiste ancora nel
                                                                     // progetto
                        Card card = new Card();// creo la nuova carta
                        card.addHistoy("TODO");// la aggiungo subito alla lista todo
                        card.setNameCard(nomecarta);
                        card.setlist("TODO");
                        card.setDescription(descrizione);
                        progetti.get(i).addIn("TODO", nomecarta);// aggiungo all lista todo del progetto la nuova carta
                        progetti.get(i).addCard(card);// aggiungo la carta alla lista delle carte
                        progetti.get(i).getListCards().add(nomecarta);
                        progetti.get(i).setListCards(progetti.get(i).getListCards());
                        progetti.get(i).setCards(progetti.get(i).getCards());
                        Progetti_Carte.get(nomeprogetto).add(nomecarta);// aggiungo la carta alla hashmap progetti-carte
                                                                        // (per il backup)
                        for (File directory : restoreCartellaProgetti.listFiles()) {
                            String prog = directory.getName();
                            if (prog.equals(nomeprogetto)) {// cerco il progetto,nella lista delle directory, a cui
                                                            // aggiungere la nuova carta
                                FileWriter newcard;
                                try {
                                    // creo il nuovo file che rappresenta la carta
                                    newcard = new FileWriter(
                                            "./WorthBackup/Progetti/" + nomeprogetto + "/" + nomecarta + ".txt");
                                    String history = card.getHistory().toString();
                                    newcard.write(history);
                                    newcard.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                        try {
                            objectMapper.writeValue(restoreProgetti, progetti);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "Carta aggiunta con successo";
                    } else
                        return "Esiste gia` una carta con questo nome";
                }
            }
        }
        return "Impossibile aggiungere la carta";

    }

    public String moveCard(String username, String nomeprogetto, String nomecarta, String listainiz, String listadest) {
        if (nomeprogetto == null || username == null || nomecarta == null || listadest == null || listainiz == null)
            throw new NullPointerException();
        boolean b = false;
        Card card = new Card();
        String resmov = "La card non puo` essere spostata!";
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                b = true; // l'utente che richiede di aggiungere la carta e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {
                if (progetti.get(i).getMemebers().contains(username)) {
                    for (int y = 0; y < progetti.get(i).getCards().size(); y++) {
                        if (progetti.get(i).getCards().get(y).getNameCard().equals(nomecarta)) {
                            card = progetti.get(i).getCards().get(y);
                            if (card.getlist().equals(listainiz)) {
                                progetti.get(i).moveCard(nomecarta, listainiz, listadest);
                                resmov = "Card spostata con successo";
                                progetti.get(i).getCards().get(y).getHistory().add(listadest);
                                progetti.get(i).getCards().get(y).setlist(listadest);
                                try {
                                    objectMapper.writeValue(restoreUtenti, utenti);
                                    objectMapper.writeValue(restoreProgetti, progetti);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else
                                return "La carta non puo` essere spostata";

                        }
                    }
                }
            }
            if (resmov.equals("Card spostata con successo")) {
                for (File directory : restoreCartellaProgetti.listFiles()) {
                    String prog = directory.getName();
                    if (prog.equals(nomeprogetto)) {
                        for (File namecard : directory.listFiles()) {
                            String cardname = namecard.getName();
                            if (cardname.equals(nomecarta + ".txt"))
                                // aggiorno il file che contiene la history della carat
                                try {
                                    namecard.delete();
                                    FileWriter replaceCard = new FileWriter(
                                            "./WorthBackup/Progetti/" + nomeprogetto + "/" + nomecarta + ".txt");
                                    String history = card.getHistory().toString();
                                    replaceCard.write(history);
                                    replaceCard.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                        }
                    }
                }
            }

        }
        return resmov;

    }

    public String getCardHistory(String nomeprogetto, String nomeCarta, String nomeutente) {
        if (nomeCarta == null || nomeprogetto == null || nomeutente == null)
            throw new NullPointerException();
        boolean b = false;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(nomeutente)) {

                b = true; // l'utente che richiede di aggiungere la carta e` registrato al
                          // servizio
            }
        }
        if (b) {
            for (int i = 0; i < progetti.size(); i++) {
                if (progetti.get(i).getMemebers().contains(nomeutente)) {
                    for (int y = 0; y < progetti.get(i).getCards().size(); y++) {
                        if (progetti.get(i).getCards().get(y).getNameCard().equals(nomeCarta)) {
                            return progetti.get(i).getCards().get(y).getHistory().toString();
                        }
                    }
                }
            }
        }
        return "Impossibile recuperare la storia della carta specificata";
    }

    public String cancelProject(String nomeprogetto, String username) {
        if (nomeprogetto == null || username == null)
            throw new NullPointerException();
        boolean b = false;
        boolean abilitato = false;
        boolean ok = true;
        boolean rimosso = false;
        int j = -1;
        // Card card = new Card();
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {

                b = true; // l'utente che richiede di rimuovere il progetto e` registrato al
                          // servizio
                j = i; // inidice dell'utente ,che richiede la rimozione del progetto
            }
        }
        if (b) {
            ArrayList<String> listprog;
            listprog = utenti.get(j).getListaProgetti();
            for (int i = 0; i < listprog.size(); i++) {
                if (listprog.get(i).equals(nomeprogetto)) {
                    abilitato = true; // l utente fa parte del progetto
                }
            }
            if (abilitato) {
                for (int i = 0; i < progetti.size(); i++) {
                    if (progetti.get(i).getProjectName().equals(nomeprogetto)) {
                        if (progetti.get(i).getListTODO().size() > 0)
                            ok = false;
                        if (progetti.get(i).getListINPROGRESS().size() > 0)
                            ok = false;
                        if (progetti.get(i).getListTOBEREVISED().size() > 0)
                            ok = false;
                    }
                }
                // rimuovo il progetto da tutte le strutture dati che lo contengono
                if (ok) {
                    String nomeprogettoinlista = new String();
                    for (int i = 0; i < progetti.size(); i++) {
                        nomeprogettoinlista = progetti.get(i).getProjectName();
                        if (nomeprogettoinlista.equals(nomeprogetto)) {
                            ConnectionINFO connectionInfo = hasmp.get(nomeprogetto);
                            connectionInfo.getMulticastSocket().close();
                            Progetti_Carte.remove(nomeprogetto);
                            hasmp.remove(nomeprogetto);
                            progetti.remove(i);
                            rimosso = true;

                        }
                    }
                    if (rimosso) {
                        ArrayList<String> listaprogetti;
                        for (int i = 0; i < utenti.size(); i++) {
                            listaprogetti = utenti.get(i).getListaProgetti();
                            for (int z = 0; z < listaprogetti.size(); z++) {
                                if (listaprogetti.get(z).equals(nomeprogetto)) {
                                    utenti.get(i).getListaProgetti().remove(nomeprogetto);

                                }
                            }
                        }
                        for (int i = 0; i < utenti_stato.size(); i++) {
                            if (utenti_stato.get(i).getNomeUtente().equals(username)) {
                                utenti_stato.get(i).getListaProgetti().remove(nomeprogetto);
                            }
                        }
                        try {
                            // rimuovo la directory che rappresenta il progetto
                            for (File directory : restoreCartellaProgetti.listFiles()) {
                                String prog = directory.getName();
                                if (prog.equals(nomeprogetto)) {
                                    for (File file : directory.listFiles())// cancello tutti i file che rappresentano le
                                                                           // card del progetto
                                        file.delete();
                                    directory.delete();
                                }
                            }
                            // aggiorno i file per il backup
                            objectMapper.writeValue(restoreUtenti, utenti);
                            objectMapper.writeValue(restoreProgetti, progetti);
                            objectMapper.writeValue(restoreConnectionInfo, hasmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "Progetto rimosso con successo!";
                    }
                }
            }
        }
        return "Impossibile rimuovere il progetto!";

    }

    // mando il messaggio nella chat del progetto specificato
    public String sendMessage(String progetto, String username, String messaggio) {
        if (progetto == null || username == null || messaggio == null)
            throw new NullPointerException();
        boolean b = false;
        boolean abilitato = false;
        int j = -1;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {
                b = true; // l'utente che richiede di rimuovere il progetto e` registrato al
                          // servizio
                j = i; // inidice dell'utente ,che richiede la rimozione del progetto
            }
        }
        if (b) {
            ArrayList<String> listprog;
            listprog = utenti.get(j).getListaProgetti();
            for (int i = 0; i < listprog.size(); i++) {
                if (listprog.get(i).equals(progetto)) {
                    abilitato = true; // l utente fa parte del progetto
                }
            }
            if (abilitato) {
                for (int y = 0; y < progetti.size(); y++) {
                    if (progetti.get(y).getProjectName().equals(progetto)) {// recupero le informazioni per le
                                                                            // connessioni dell chat
                        String ipAddress = progetti.get(y).getIpAddress();
                        int port = progetti.get(y).getProjectPort();
                        String tosend = ipAddress + " " + port;
                        return tosend;
                    }

                }

            }
        }
        return "Impossibile inviare il  messaggio!";

    }

    public String readMessage(String progetto, String username) {
        if (progetto == null || username == null)
            throw new NullPointerException();
        boolean b = false;
        boolean abilitato = false;
        int j = -1;
        for (int i = 0; i < utenti.size(); i++) {
            if (utenti.get(i).getNickName().equals(username)) {

                b = true; // l'utente che richiede di leggere i messaggi e` registrato al
                          // servizio
                j = i; // inidice dell'utente ,che richiede la lettura dei messaggi
            }
        }
        if (b) {
            ArrayList<String> listprog;
            listprog = utenti.get(j).getListaProgetti();
            for (int i = 0; i < listprog.size(); i++) {
                if (listprog.get(i).equals(progetto)) {
                    abilitato = true; // l utente fa parte del progetto
                }
            }
            if (abilitato) {
                for (int y = 0; y < progetti.size(); y++) {
                    if (progetti.get(y).getProjectName().equals(progetto)) {// recupero le informazioni per le
                                                                            // connessioni dell chat
                        String ipAddress = progetti.get(y).getIpAddress();
                        int port = progetti.get(y).getProjectPort();
                        String tosend = ipAddress + " " + port;
                        return tosend;
                    }

                }

            }
        }
        return "Impossibile leggere i messaggi!";
    }

    public synchronized void registerForCallback(NotifyInterface ClientInterface) throws RemoteException {
        if (!clients.contains(ClientInterface)) {
            clients.add(ClientInterface);
            System.out.println("Nuovo client registrato");
        }
    }

    public synchronized void unregisterForCallback(NotifyInterface Client) throws RemoteException {
        if (clients.remove(Client)) {
            System.out.println("Client unregistered");
        }
    }

    public synchronized void doCallbacks(ArrayList<Utente_Stato> val) throws RemoteException {
        if (val == null)
            throw new NullPointerException();
        System.out.println("Inizia la callback per aggiornare lo stato degli utenti");
        Iterator i = clients.iterator();
        while (i.hasNext()) {
            NotifyInterface client = (NotifyInterface) i.next();
            for (int j = 0; j < utenti_stato.size(); j++) {
                String username = utenti_stato.get(j).getNomeUtente();
                String stato = utenti_stato.get(j).getStato();
                client.notifyEvent(username, stato);

            }
        }
        System.out.println("Callbacks completa");
    }

    // notifica di un aggiornamento
    // quando viene richiamato, fa il callback a tutti i client
    public void update(ArrayList<Utente_Stato> val) throws RemoteException {
        if (val == null)
            throw new NullPointerException();
        doCallbacks(val);
    }

    public synchronized void doCallbacksProjectList(ArrayList<Utente_Stato> val) throws RemoteException {
        if (val == null)
            throw new NullPointerException();
        System.out.println("Inizia la callback per aggiornare la lista dei progetti");
        Iterator i = clients.iterator();
        while (i.hasNext()) {
            NotifyInterface client = (NotifyInterface) i.next();
            for (int j = 0; j < val.size(); j++) {
                String username = val.get(j).getNomeUtente();
                int size = val.get(j).getListaProgetti().size();
                if (size > 0) {
                    String prog = val.get(j).getListaProgetti().get(size - 1);
                    client.notifyEvent2(username, prog);
                }
            }
        }
        System.out.println("Callbacks complete");
    }

    // per aggiornare la lista dei progetti
    public void updateProjectList(ArrayList<Utente_Stato> val) throws RemoteException {
        if (val == null)
            throw new NullPointerException();
        doCallbacksProjectList(val);
    }

    public synchronized void doCallbacksHashMap(HashMap<String, ConnectionINFO> ms) {
        if (ms == null)
            throw new NullPointerException();
        System.out.println("Callback hashmap");
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
        System.out.println("Callback hashmap completa");
    }

    public void updateHashMap(HashMap<String, ConnectionINFO> ms) {
        if (ms == null)
            throw new NullPointerException();
        doCallbacksHashMap(ms);
    }
}
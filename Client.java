import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteObject;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Client extends RemoteObject implements NotifyInterface {

    private static final long serialVersionUID = 1L;

    private int RMI_PORT = 6000;
    private int TCP_PORT = 60000;
    private final InetAddress multicastAddress;
    private static final String ServerAdd = "127.0.0.5";
    private String name = new String();
    private final HashMap<String, ConnectionINFO> listaaddrress;
    private final HashMap<String, Chat> Progetti_Chat;
    private int dimensionelista = 0;

    private ArrayList<Utente_Stato> utenti_stato;

    public Client(String addr, int port)
            throws UnknownHostException, IllegalArgumentException, ConnectException, RemoteException {
        super();
        this.multicastAddress = InetAddress.getByName(addr);
        listaaddrress = new HashMap<>();
        Progetti_Chat = new HashMap<>();
        if (!this.multicastAddress.isMulticastAddress())
            throw new IllegalArgumentException();
        utenti_stato = new ArrayList<Utente_Stato>();
    }

    public void start() {
        SocketChannel socket;
        try {

            // ottiene una reference per il registro
            Registry r = LocateRegistry.getRegistry(RMI_PORT);
            Registry reg = LocateRegistry.getRegistry(3400);
            // ottiene una reference al server
            RMI_Interface rmi = (RMI_Interface) r.lookup("SERVER_RMI");
            RegisterCallbackInterface rc = (RegisterCallbackInterface) reg.lookup("CLIENT_REGISTER");
            NotifyInterface callbackObj = this;
            NotifyInterface stub = (NotifyInterface) UnicastRemoteObject.exportObject(callbackObj, 0);

            // creazione e apertura della socket
            Scanner in = new Scanner(System.in);
            socket = SocketChannel.open();
            socket.connect(new InetSocketAddress(ServerAdd, TCP_PORT));
            System.out.println("-----Benvenuti in Worth!-----");
            System.out.println("Per conoscere le operazioni digitare il comando Help");
            String operazione;
            String status_client = "Client libero";

            while (true) {
                System.out.print("Prossima operazione: ");
                aggiornaUtente();
                operazione = in.nextLine();
                if (operazione.equals("Registrazione")) {
                    System.out.print("Username da registrare: ");
                    String username = in.nextLine();
                    System.out.print("Password: ");
                    String password = in.nextLine();
                    if (username.isBlank())
                        System.out.println("Username non valido!");
                    if (password.isBlank())
                        System.out.println("Password non valida");
                    else {
                        String res = rmi.registerUser(username, password);
                        Utente_Stato newutente = new Utente_Stato();
                        newutente.setNomeUtente(username);
                        newutente.setStato("offline");
                        utenti_stato.add(newutente);
                        if (!res.equals("Utente gia` registrato"))
                            rc.registerForCallbackAtRegister(stub);
                        System.out.println(res);
                    }
                }
                if (operazione.equals("Login")) {
                    if (status_client.equals("Client libero")) {
                        System.out.print("Username: ");
                        String username = in.nextLine();
                        System.out.print("Password: ");
                        String password = in.nextLine();
                        if (username.isBlank())
                            System.out.println("Username non valido");
                        if (password.isBlank())
                            System.out.println("Password non valida");
                        else {
                            String send = operazione + " " + username + " " + password;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            rmi.registerForCallback(stub);
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();

                            if (res.equals("Login effettuato")) {
                                name = username;
                                status_client = "Client occupato";
                                for (String np : Progetti_Chat.keySet()) { // avvio le chat dei progetti di cui l'utente
                                                                           // e` membro
                                    Chat chat = Progetti_Chat.get(np);
                                    new Thread(chat).start();
                                }

                            } else
                                rmi.unregisterForCallback(stub);
                            System.out.println(res);
                        }
                    } else {
                        System.out.println("Client occupato da un altro utente. Eseguire prima il Logout");
                    }
                }
                if (operazione.equals("Logout")) {
                    if (status_client.equals("Client occupato")) {
                        String send = operazione + " " + name;
                        ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                        socket.write(buffer);
                        buffer.clear();
                        buffer.flip();
                        buffer = ByteBuffer.allocate(1024);
                        socket.read(buffer);
                        buffer.flip();
                        String res = StandardCharsets.UTF_8.decode(buffer).toString();
                        buffer.clear();
                        buffer.flip();
                        if (res.equals("Logout effettuato")) {
                            status_client = "Client libero";
                            rmi.unregisterForCallback(stub);
                            rc.unregisterForCallbackAtLogout(stub);
                            in.close();
                            for (int i = 0; i < utenti_stato.size(); i++) {
                                if (utenti_stato.get(i).getNomeUtente().equals(name)) {
                                    for (int j = 0; j < utenti_stato.get(i).getListaProgetti().size(); j++) {
                                        String nomeprog = utenti_stato.get(i).getListaProgetti().get(j);
                                        Progetti_Chat.get(nomeprog).closeChat();
                                    }
                                }
                            }
                            socket.close(); // chiusura socket
                            System.out.println("Logout effettuato");
                            break;
                        } else
                            System.out.println(res);
                    } else
                        System.out.println("Nessun utente loggato! Logout fallito");
                }
                if (operazione.equals("ListaUtenti")) {
                    if (status_client.equals("Client occupato")) {
                        boolean almenouno = false;
                        System.out.println("Utenti:");
                        for (int i = 0; i < utenti_stato.size(); i++) {
                            almenouno = true;
                            System.out.print(utenti_stato.get(i).getNomeUtente() + " ");
                            System.out.println(utenti_stato.get(i).getStato() + " ");
                        }
                        if (!almenouno)
                            System.out.println("Non ci sono utenti!");
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("UtentiOnline")) {
                    if (status_client.equals("Client occupato")) {
                        boolean almenouno = false;
                        System.out.println("Utenti Online:");
                        for (int i = 0; i < utenti_stato.size(); i++) {
                            if (utenti_stato.get(i).getStato().equals("online")) {
                                almenouno = true;
                                System.out.print(utenti_stato.get(i).getNomeUtente() + " ");
                            }
                        }
                        if (!almenouno)
                            System.out.print("Nessun utente Online!");
                        System.out.println();
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("ListaProgetti")) {
                    if (status_client.equals("Client occupato")) {
                        String send = operazione + " " + name;
                        ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                        socket.write(buffer);
                        buffer.clear();
                        buffer.flip();
                        buffer = ByteBuffer.allocate(1024);
                        socket.read(buffer);
                        buffer.flip();
                        String res = StandardCharsets.UTF_8.decode(buffer).toString();
                        buffer.clear();
                        buffer.flip();
                        if (res != null) {
                            System.out.println("Lista dei progetti di cui sei membro:");
                            System.out.println(res);
                        } else {
                            System.out.println("Errore nella richiesta della lista dei progetti");
                        }
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("CreaProgetto")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.println("Creazione di un nuovo progetto");
                        System.out.print("Nome progetto: ");
                        String nomeprogetto = in.nextLine();
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            String send = operazione + " " + name + " " + nomeprogetto;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            if (res.equals("Progetto creato")) {
                                // Creo lo chat da associare al progetto creato
                                ConnectionINFO connectioInfo = listaaddrress.get(nomeprogetto);
                                String address = connectioInfo.getAddress();
                                InetAddress ia = InetAddress.getByName(address);
                                int port = connectioInfo.getPort();
                                String nomeProgetto = connectioInfo.getNomeProgetto();
                                MulticastSocket ms = new MulticastSocket(port);
                                connectioInfo.setMulticastSocket(ms);
                                Chat newChat = new Chat(ia, port, nomeProgetto);
                                newChat.setMulticastSocket(ms);
                                new Thread(newChat).start(); // avvio del thread in background per ricevere i messaggi
                                                             // della chat
                                ms.joinGroup(ia);
                                Progetti_Chat.put(nomeprogetto, newChat);// aggiorno la hashmap che contiene le
                                                                         // associazioni progetti-chat
                            }
                            System.out.println(res);
                            buffer.clear();
                            buffer.flip();
                        }
                    } else
                        System.out.println("Nessun utente loggato! Il progetto non puo` essere creato");
                }
                if (operazione.equals("AggiungiMembro")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.println("Aggiunta di un nuovo membro al progetto!");
                        System.out.print("Nome membro da aggiunere: ");
                        String nomeutente = in.nextLine();
                        System.out.print("Nome del progetto a cui aggiunere l'utente: ");
                        String nomeprogetto = in.nextLine();
                        if (nomeutente.isBlank())
                            System.out.println("Nome utente non valido!");
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido");
                        else {
                            String send = operazione + " " + nomeutente + " " + nomeprogetto + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato!Il nuovo membro non puo` essere aggiunto");
                }
                if (operazione.equals("MostraMembri")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome del progetto di cui vedere la lista dei membri: ");
                        String nomeprogetto = in.nextLine();
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            String send = operazione + " " + nomeprogetto + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("MostraCarte")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome progetto: ");
                        String nomeProgetto = in.nextLine();
                        if (nomeProgetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            String send = operazione + " " + nomeProgetto + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("MostraCarta")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome della carta: ");
                        String nomecarta = in.nextLine();
                        System.out.print("Apprtenente al progetto: ");
                        String nomeProgetto = in.nextLine();
                        if (nomecarta.isBlank())
                            System.out.println("Nome carta non valido!");
                        if (nomeProgetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            String send = operazione + " " + nomeProgetto + " " + nomecarta + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! La carta non puo` essere recuperata");
                }
                if (operazione.equals("AggiungiCarta")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome della carta da aggiungere: ");
                        String nomecarta = in.nextLine();
                        System.out.print("Nome del progetto a cui aggiungere la carta: ");
                        String nomeprogetto = in.nextLine();
                        System.out.print("Descrizione carta: ");
                        String descrizione = in.nextLine();
                        if (nomecarta.isBlank())
                            System.out.println("Nome carta non valido!");
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        if (descrizione.isBlank())
                            System.out.println("Descrizione non valida!");
                        else {
                            String send = operazione + " " + nomeprogetto + " " + nomecarta + " " + name + " "
                                    + descrizione;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere aggiunta");
                }
                if (operazione.equals("SpostaCarta")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome della carta da spostare: ");
                        String nomecarta = in.nextLine();
                        System.out.print("Nome del progetto: ");
                        String nomeProgetto = in.nextLine();
                        System.out.print("Lista di partenza: ");
                        String listainiz = in.nextLine();
                        System.out.print("Lista destinazione: ");
                        String listades = in.nextLine();
                        if (nomecarta.isBlank())
                            System.out.println("Nome carta non valido!");
                        if (nomeProgetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        if (listainiz.isBlank())
                            System.out.println("Lista iniziale non valida!");
                        if (listades.isBlank())
                            System.out.println("Lista destinazione non valida!");
                        else {
                            String send = operazione + " " + nomecarta + " " + nomeProgetto + " " + listainiz + " "
                                    + listades + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            if (res.equals("Card spostata con successo")) {
                                // invio un messaggio nella chat del progetto a cui la carta appartiene per
                                // notificare lo spostamento
                                ConnectionINFO connectionInfo = listaaddrress.get(nomeProgetto);
                                String messaggio = nomecarta + " spostata nella lista " + listades;
                                MulticastSocket ms = connectionInfo.getMulticastSocket();
                                Chat chat = Progetti_Chat.get(nomeProgetto);
                                chat.setMulticastSocket(ms);
                                chat.inviaMessaggio("System: ", messaggio);
                            }
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! La lista non puo` essere recuperata");
                }
                if (operazione.equals("CronologiaCarta")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome del progetto: ");
                        String nomeprogetto = in.nextLine();
                        System.out.print("Nome della cart: ");
                        String nomecarta = in.nextLine();
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        if (nomecarta.isBlank())
                            System.out.println("Nome carta non valido!");
                        else {
                            String send = operazione + " " + nomeprogetto + " " + nomecarta + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! Il progetto non puo` essere cancellato");

                }
                if (operazione.equals("CancellaProgetto")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Progetto da cancellare: ");
                        String nomeprogetto = in.nextLine();
                        if (nomeprogetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            String send = operazione + " " + nomeprogetto + " " + name;
                            ByteBuffer buffer = ByteBuffer.wrap(send.getBytes());
                            socket.write(buffer);
                            buffer.clear();
                            buffer.flip();
                            buffer = ByteBuffer.allocate(1024);
                            socket.read(buffer);
                            buffer.flip();
                            String res = StandardCharsets.UTF_8.decode(buffer).toString();
                            buffer.clear();
                            buffer.flip();
                            if (res.equals("Progetto rimosso con successo!")) {
                                dimensionelista = dimensionelista - 1;
                                for (int i = 0; i < utenti_stato.size(); i++) {
                                    if (utenti_stato.get(i).getNomeUtente().equals(name)) {
                                        utenti_stato.get(i).getListaProgetti().remove(nomeprogetto);
                                    }
                                }
                                // rimuovo il progetto da tutte le strutture dati
                                listaaddrress.remove(nomeprogetto);
                                Progetti_Chat.get(nomeprogetto).closeChat();
                                Progetti_Chat.remove(nomeprogetto);
                            }
                            System.out.println(res);
                        }
                    } else
                        System.out.println("Nessun utente loggato! Il progetto non puo` essere cancellato");

                }
                if (operazione.equals("LeggiMessaggi")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome progetto dalla quale leggere i messaggi: ");
                        String progetto = in.nextLine();
                        if (progetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        else {
                            boolean membro = false;
                            for (int i = 0; i < utenti_stato.size(); i++) {
                                if (utenti_stato.get(i).getNomeUtente().equals(name)) {
                                    ArrayList<String> listaProgetti = utenti_stato.get(i).getListaProgetti();
                                    for (int j = 0; j < listaProgetti.size(); j++) {
                                        if (listaProgetti.get(j).equals(progetto)) {
                                            membro = true;

                                            Chat chat = Progetti_Chat.get(progetto);
                                            if (chat != null)
                                                chat.getMessaggiChat();
                                        }
                                    }
                                    if (!membro)
                                        System.out.println("Non fai parte di questo progetto");
                                }
                            }
                        }
                    } else
                        System.out.println("Nessun utente loggato!Impossibile inviare il messaggio");
                }
                if (operazione.equals("InviaMessaggio")) {
                    if (status_client.equals("Client occupato")) {
                        System.out.print("Nome del progetto dove inviare il messaggio: ");
                        String progetto = in.nextLine();
                        System.out.print("Messaggio da inviare: ");
                        String messaggio = in.nextLine();
                        if (progetto.isBlank())
                            System.out.println("Nome progetto non valido!");
                        if (messaggio.isBlank())
                            System.out.println("Formato messaggio non valido!");
                        else {
                            boolean membro = false;
                            for (int i = 0; i < utenti_stato.size(); i++) {
                                if (utenti_stato.get(i).getNomeUtente().equals(name)) {
                                    for (int j = 0; j < utenti_stato.get(i).getListaProgetti().size(); j++) {
                                        if (utenti_stato.get(i).getListaProgetti().get(j).equals(progetto)) {
                                            membro = true;
                                            Chat chat = Progetti_Chat.get(progetto);
                                            if (chat != null) {
                                                chat.inviaMessaggio(name, messaggio);
                                            }

                                        }
                                    }
                                    if (!membro)
                                        System.out.println("Non fai parte di questo progetto");
                                }
                            }
                        }
                    } else
                        System.out.println("Nessun utente loggato!Impossibile inviare il messaggio");
                }
                if (operazione.equals("Help")) {
                    System.out.println("Ecco la lista delle operazioni possibili:");
                    System.out.println("Registrazione -> registra un nuovo utente al servizio");
                    System.out.println("Login -> accede al servizio mediante le credenziali");
                    System.out.println("Logout -> esegue il logout dell'uente collegato");
                    System.out.println("ListaUtenti -> restituisce la lista degli utenti registrati al servizio");
                    System.out.println("UtentiOnline -> restituisce la lista degli utenti Online");
                    System.out.println("ListaProgetti -> resituisce la lista dei progetti di cui l'utente e` membro");
                    System.out.println("CreaProgetto -> crea un nuovo progetto");
                    System.out.println("AggiungiMembro -> aggiunge un nuovo membro al progetto specificato");
                    System.out.println("MostraMembri -> mostra la lista dei membri del progetto specificato");
                    System.out.println("MostraCarte -> mostra la lista delle carte del progetto specificato");
                    System.out.println("MostraCarta -> mostra il nome e lo stato di una specifica carta");
                    System.out.println("AggiungiCarta -> aggiunge una nuova carta a un progetto");
                    System.out.println("SpostaCarta -> sposta una carta dalla lista di partenza a quella di arrivo");
                    System.out.println("NB:digitare i nomi della lista di partenza e arrivo con caratteri MAIUSCOLI");
                    System.out.println("CronologiaCarta -> mostra la cronologia della carta");
                    System.out.println("CancellaProgetto -> elimina un progetto");
                    System.out.println(
                            "LeggiMessaggi -> mostra i messaggi di un progetto dal momento in cui si esegue il login");
                    System.out.println("InviaMessaggio -> invia un messaggio nella chat di un processo");
                    System.out.println("NB: ogni comando deve essere digitato essattamente come si vede scritto!");
                }
            }
            UnicastRemoteObject.unexportObject(callbackObj, true);
        } catch (ConnectException e) {
            e.printStackTrace();
            System.err.println("Ops! Errore durante la connesione con il server. Verificare che il Server sia attivo!");
        } catch (RemoteException e) {
            e.printStackTrace();
            System.err.println("Errore remote object");
        } catch (NotBoundException e) {
            e.printStackTrace();
            System.err.println("lookup exception");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Socket exception");
        }
    }

    // Aggiorno le informazioni riguardo l'utente collegato in un particolare
    // client, in particolare vengono aggiornate le struture dati dell'utente grazie
    // agli aggiornamenti che il client riceve con i meccanismi delle notifiche
    public void aggiornaUtente() throws IOException {
        for (int i = 0; i < utenti_stato.size(); i++) {
            if (utenti_stato.get(i).getNomeUtente().equals(name)) {
                if (utenti_stato.get(i).getListaProgetti().size() > dimensionelista) {
                    dimensionelista++;
                    int size = utenti_stato.get(i).getListaProgetti().size();
                    if (size > 0) {
                        String prog = utenti_stato.get(i).getListaProgetti().get(size - 1);
                        ConnectionINFO connectInfo = listaaddrress.get(prog);
                        if (connectInfo != null) {
                            if (!Progetti_Chat.containsKey(prog)) {
                                InetAddress ia = InetAddress.getByName(connectInfo.getAddress());
                                MulticastSocket ms = connectInfo.getMulticastSocket();
                                ms.joinGroup(ia);
                                int port = connectInfo.getPort();
                                Chat newChat = new Chat(ia, port, name);
                                newChat.setMulticastSocket(ms);
                                new Thread(newChat).start();
                                Progetti_Chat.put(prog, newChat);
                            }
                        }
                    }
                }
            }
        }
    }

    // metodo che aggiorna la lista dei progetti degli utenti
    public void notifyEvent2(String username, String prog) throws RemoteException {
        for (int i = 0; i < utenti_stato.size(); i++) {
            if (utenti_stato.get(i).getNomeUtente().equals(username)) {
                if (!utenti_stato.get(i).getListaProgetti().contains(prog))
                    utenti_stato.get(i).getListaProgetti().add(prog);
            }
        }
    }

    // metodo che notifica ai client se un utente ha effettuato il login o il
    // logout.
    public void notifyEvent(String username, String stato) throws RemoteException {
        boolean giaregistrato = false;
        int j = -1;
        for (int i = 0; i < utenti_stato.size(); i++) {
            if (utenti_stato.get(i).getNomeUtente().equals(username)) {
                giaregistrato = true;
                j = i;
            }
        }
        if (giaregistrato) {
            utenti_stato.get(j).setStato(stato);
        }
        if (!giaregistrato) {
            Utente_Stato newutente = new Utente_Stato();
            newutente.setNomeUtente(username);
            newutente.setStato(stato);
            utenti_stato.add(newutente);
        }
    }

    // metodo che aggiorna le associazioni Progetti-Informazioni di connessione per
    // interagire con le chat
    public void notifyEvent3(String nomeprogetto, ConnectionINFO connectionINFO) throws RemoteException, IOException {
        if (connectionINFO.getMulticastSocket() == null) {
            MulticastSocket ms = new MulticastSocket(connectionINFO.getPort());
            connectionINFO.setMulticastSocket(ms);
        }
        listaaddrress.putIfAbsent(nomeprogetto, connectionINFO);
    }

    // metodo che aggiorna la lista dei progetti degli utenti quando viene eliminato
    // un prozetto
    public void notifyEvent4(String nomeprogetto) {
        if (Progetti_Chat.containsKey(nomeprogetto)) {
            dimensionelista--;
            Progetti_Chat.get(nomeprogetto).closeChat();
        }
        this.listaaddrress.remove(nomeprogetto);
        for (int i = 0; i < utenti_stato.size(); i++) {
            utenti_stato.get(i).getListaProgetti().remove(nomeprogetto);
        }
    }
}

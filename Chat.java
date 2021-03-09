import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import java.util.ArrayList;

//La classe Chat e` una classe di tipo Runnable che, viene mandata in esecuzione in background
// non appena viene creato un progetto (e quindi la relativa Chat) o un utente
// viene aggiunto a un progetto,in modo da ricevere e/o inviare messaggi a 
//tutti gli utenti che fanno parte del progetto
public class Chat implements Runnable {
    private InetAddress address;
    private int port;
    private String nomeProgetto;
    private ArrayList<String> messaggiChat;
    private MulticastSocket multicastSocket;

    public Chat(InetAddress address, int port, String nomeProgetto) {
        this.address = address;
        this.nomeProgetto = nomeProgetto;
        this.port = port;

        messaggiChat = new ArrayList<>();
    }

    // set del MulticastSocket associata alla Chat
    public void setMulticastSocket(MulticastSocket ms) {
        this.multicastSocket = ms;
    }

    // ottiene il MulticastSocket associata alla Chat
    public MulticastSocket getMulticastSocket() {
        return this.multicastSocket;
    }

    // ottiene l'InetAddress associata alla Chata
    public InetAddress getInetAddress() {
        return this.address;
    }

    // ottiene il nome del progetto legato alla Chat
    public String getNomeProgetto() {
        return this.nomeProgetto;
    }

    // ottiene il numero della porta associato alla Chat
    public int getPorta() {
        return this.port;
    }

    // Stampa i messaggi ricevuti dal momento del login la prima volta che viene
    // eseguito,dopodiche` stampa i messaggi ricevuti dall'ultima esecuzione
    public void getMessaggiChat() {
        ArrayList<String> newMessaggi = new ArrayList<>();
        for (int i = 0; i < messaggiChat.size(); i++) {
            String messaggio = messaggiChat.get(i);
            newMessaggi.add(messaggio);
            System.out.println(messaggio);
        }
        System.out.println("Non ci sono altri messaggi");
        messaggiChat.clear();
    }

    // Invia un messaggio alla Chat del progetto. Utilizzato anche per mandare i
    // messaggi da parte del sistema per notificare lo spostamento di una carta
    public void inviaMessaggio(String utente, String messaggio) {
        String tosend = utente + ": " + messaggio;
        try {
            DatagramPacket datagramPacket = new DatagramPacket(tosend.getBytes(), tosend.length(), this.address,
                    this.port);
            multicastSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (utente != "System: ")
            System.out.println("Messaggio Inviato!");
    }

    // Chiude la MulticastSocket associata alal Chat
    public void closeChat() {
        if (multicastSocket != null) {
            if (!multicastSocket.isClosed()) {
                multicastSocket.close();
            }
            multicastSocket.disconnect();
            multicastSocket.close();

        }
    }

    @Override
    public void run() {
        while (true) {
            if (multicastSocket.isClosed())
                break;
            byte[] buf = new byte[512];
            try {
                DatagramPacket msgPacket = new DatagramPacket(buf, buf.length);
                multicastSocket.receive(msgPacket);
                String msg = new String(msgPacket.getData());
                messaggiChat.add(msg);
            } catch (IOException e) {

            }
        }
    }
}

import java.io.Serializable;
import java.net.MulticastSocket;

import com.fasterxml.jackson.annotation.JsonIgnore;

//La classe ConnectionINFO e` stata realizzata per mantenere,in un unico oggetto,
// tutte le informazioni riguardo una connesione MulticastSocket
public class ConnectionINFO implements Serializable {

    private static final long serialVersionUID = 1L;
    private transient MulticastSocket ms;
    private String address;
    private int port;
    private String nomeProgetto;

    // set del MulticastSocket
    @JsonIgnore
    public void setMulticastSocket(MulticastSocket ms) {
        this.ms = ms;
    }

    // set della stringa utilizzata per ottenere l'InetAddress
    public void setAddress(String address) {
        this.address = address;
    }

    // set del numero di porta
    public void setPort(int port) {
        this.port = port;
    }

    // set del nome del progetto che e` associato alla connessione di cui si vuol
    // tenere traccia
    public void setNomeProgetto(String nomeProgetto) {
        this.nomeProgetto = nomeProgetto;
    }

    // ottiene la MulticastSocket
    @JsonIgnore
    public MulticastSocket getMulticastSocket() {
        return this.ms;
    }

    // ottiene la stringa utile per ottenere l'InetAddress
    public String getAddress() {
        return this.address;
    }

    // ottiene il numero di porta
    public int getPort() {
        return this.port;
    }

    // ottiene il nome del progetto associato alla connessione
    public String getNomeProgetto() {
        return this.nomeProgetto;
    }
}

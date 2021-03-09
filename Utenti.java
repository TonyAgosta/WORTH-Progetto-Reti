//Tony Agosta 544090

import java.util.ArrayList;

//La classe Utenti e` stata realizzata per contenere tutte le informazioni
// relative a un singolo utente : Nickname, password, Stato(online/offline)
//e lista (di tipo String) dei progetti
public class Utenti {
    private String nickname;
    private String password;
    private String stato;
    private ArrayList<String> ListaProgetti;

    public Utenti() {
        nickname = new String();
        password = new String();
        stato = new String();
        ListaProgetti = new ArrayList<String>();
    }

    // set dello stato (online,offline) dell'utente
    public void setStatus(String status) {
        this.stato = status;
    }

    // set della password dell utente
    public void setPassword(String password) {
        this.password = password;
    }

    // set del nome dell'utente
    public void setNickName(String nickname) {
        this.nickname = nickname;
    }

    // riceve come parametro la lista dei progetti aggiornata e sostituisce la
    // vecchia versione della lista dei progetti associata all'utente
    public void setListaProgetti(ArrayList<String> progetti) {
        this.ListaProgetti = progetti;
    }

    // ottiene il nome dell'utente
    public String getNickName() {
        return this.nickname;
    }

    // ottiene lo stato (online,offline) dell'utente
    public String getStatus() {
        return this.stato;
    }

    // ottiene la password dell'utente
    public String getPassword() {
        return this.password;
    }

    // ottiene la lista dei progetti associato all'utente
    public ArrayList<String> getListaProgetti() {
        return this.ListaProgetti;
    }

    // aggiunge un progetto alla lista dei progetti
    public void addProgetto(String progetto) {
        this.ListaProgetti.add(progetto);
    }

    // ottiene il nome di un progetto specifico. Viene utilizzato per verificare se
    // l'utente fa parte di quel progetto
    public String getProgetto(String nomeprogetto) {
        for (int i = 0; i < ListaProgetti.size(); i++) {
            if (ListaProgetti.get(i).equals(nomeprogetto))
                return ListaProgetti.get(i);
        }
        return null;
    }

}

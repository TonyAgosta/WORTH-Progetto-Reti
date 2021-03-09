import java.util.ArrayList;

//La classe Utente_stato e` stata realizzata per creare e aggiornare,tramite RMI,
//la struttura dati del client. Contiene il nome dell'utente che fa il Login in un client,
//lo stato (online,offline) dell'utente e la lista(di tipo stringa) di progetti
//di cui l'utente e` membro
public class Utente_Stato {
    private String nomeutente;
    private String stato;
    private ArrayList<String> project_list;

    public Utente_Stato() {
        project_list = new ArrayList<String>();
    }

    // set del nomeutente
    public void setNomeUtente(String nomeutente) {
        this.nomeutente = nomeutente;
    }

    // set dello stato (online/offline)
    public void setStato(String stato) {
        this.stato = stato;
    }

    // ottiene il nome utente
    public String getNomeUtente() {
        return this.nomeutente;
    }

    // ottiene lo stato
    public String getStato() {
        return this.stato;
    }

    // aggiorna lo stato dell'utente: da online a offline e viceversa
    public void aggiorna(Utenti utente, String stato) {
        utente.setStatus(stato);
    }

    // aggiunge un nuovo progetto alla lista (di tipo String) associata all'utente
    public void aggironalistaprogetti(String prog) {
        project_list.add(prog);

    }

    // ottiene la lista (di tipo stringa) associata all'utente
    public ArrayList<String> getListaProgetti() {
        return this.project_list;
    }

}

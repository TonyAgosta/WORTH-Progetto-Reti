import java.util.ArrayList;

public class Progetto {
    private ArrayList<String> users;
    private String projectName;
    private ArrayList<String> nameCards;
    private ArrayList<Card> cards;
    private ArrayList<String> TODO;
    private ArrayList<String> INPROGRESS;
    private ArrayList<String> TOBEREVISED;
    private ArrayList<String> DONE;
    private String ipAddress;
    private int project_port;

    public Progetto() {
        ipAddress = new String();
        projectName = new String();
        users = new ArrayList<String>();
        nameCards = new ArrayList<String>();
        cards = new ArrayList<Card>();
        TODO = new ArrayList<String>();
        INPROGRESS = new ArrayList<String>();
        TOBEREVISED = new ArrayList<String>();
        DONE = new ArrayList<String>();
    }

    // set il numero di porta associato al progetto
    public void setProjectPort(int port) {
        this.project_port = port;
    }

    // set il nome del progetto
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    // set del valore della stringa utilizzata per ottenere l'InetAddress
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    // aggiorna la lista dei membri del progetto
    public void setMembers(ArrayList<String> members) {
        this.users = members;
    }

    // aggiorna la lista (di tipo card) delle carte del progetto
    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    // aggiorna la lista dei nomi delle carte del progetto
    public void setListCards(ArrayList<String> listCards) {
        this.nameCards = listCards;
    }

    // ottiene la stringa utile per ottenere l'InetAddress
    public String getIpAddress() {
        return this.ipAddress;
    }

    // ottiene il numero di porta associato al progetto
    public int getProjectPort() {
        return this.project_port;
    }

    // ottiene il nome del progetto
    public String getProjectName() {
        return this.projectName;
    }

    // ottiene la lista dei membri associata al progetto
    public ArrayList<String> getMemebers() {
        return this.users;
    }

    // ottiene il nome e lo stato di una specifica carta
    public String getCard(String card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getNameCard().equals(card)) {
                return "Nome: " + cards.get(i).getNameCard() + ",Stato: " + cards.get(i).getlist() + ",Descrzione: "
                        + cards.get(i).getDescription();
            }
        }
        return null;
    }

    // aggiunge un membro alla lista dei membri del progetto
    public void addMembers(String user) {
        users.add(user);
    }

    // aggiunge una Carta alla lista delle carte associata al progetto
    public void addCard(Card card) {
        this.cards.add(card);
    }

    // ottiene la lista dei nomi delle carte che appartengono al progetto
    public ArrayList<String> getListCards() {
        return nameCards;
    }

    // ottiene la lista (di tipo Card) delle carte che appartengono al progetto
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    // aggiunge il nome di una carta in un una delle possibili liste che indicano lo
    // stato in cui si trovano le carte. Usato nel metodo "MoveCard"
    public void addIn(String list, String card) {
        if (list.equals("TODO"))
            TODO.add(card);
        if (list.equals("INPROGRESS"))
            INPROGRESS.add(card);
        if (list.equals("TOBEREVISED"))
            TOBEREVISED.add(card);
        if (list.equals("DONE"))
            DONE.add(card);
    }

    // rimuove il nome di una carta da una specifica lista. Usato nel metodo
    // "MoveCard"
    public void removeFrom(String list, String card) {
        if (list.equals("TODO"))
            TODO.remove(card);
        if (list.equals("INPROGRESS"))
            INPROGRESS.remove(card);
        if (list.equals("TOBEREVISED"))
            TOBEREVISED.remove(card);
        if (list.equals("DONE"))
            DONE.remove(card);
    }

    // Sposta una carat da una lista di partenza a una di destinazione, rispettando
    // i vincoli di movimento
    public boolean moveCard(String card, String listainiz, String listadest) {
        if (listainiz.equals("TODO") && listadest.equals("INPROGRESS")) {
            this.removeFrom(listainiz, card);
            this.addIn(listadest, card);
            return true;
        }
        if (listainiz.equals("INPROGRESS") && ((listadest.equals("TOBEREVISED") || listadest.equals("DONE")))) {
            this.removeFrom(listainiz, card);
            this.addIn(listadest, card);
            return true;
        }
        if (listainiz.equals("TOBEREVISED") && ((listadest.equals("TOBEREVISED") || listadest.equals("DONE")))) {
            this.removeFrom(listainiz, card);
            this.addIn(listadest, card);
            return true;
        }
        return false;
    }

    // restituisce la lista delle carte che si trovano nello stato "todo"
    public ArrayList<String> getListTODO() {
        return this.TODO;
    }

    // restituisce la lista delle carte che si trovano nello stato "inprogress"
    public ArrayList<String> getListINPROGRESS() {
        return this.INPROGRESS;

    }

    // restituisce la lista delle carte che si trovano nello stato "toberevised"
    public ArrayList<String> getListTOBEREVISED() {
        return this.TOBEREVISED;
    }

    // // restituisce la lista delle carte che si trovano nello stato "done"
    public ArrayList<String> getListDONE() {
        return this.DONE;
    }

}
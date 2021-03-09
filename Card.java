import java.util.ArrayList;

public class Card {
    private String nameCard;
    private String description;
    private ArrayList<String> history;
    private String list;

    public Card() {
        this.history = new ArrayList<String>();
    }

    // set del nome della carta
    public void setNameCard(String nameCard) {
        this.nameCard = nameCard;
    }

    // ottiene il nome della carta
    public String getNameCard() {
        return nameCard;
    }

    // set della descrizione della carta
    public void setDescription(String description) {
        this.description = description;
    }

    // ottiene la descrizione associata alla carta
    public String getDescription() {
        return this.description;
    }

    // set della lista in cui si trova la carta (todo,inprogess,toberevised,done)
    public void setlist(String list) {
        this.list = list;
    }

    // ottine il nome della lista in cui si trova la carta
    public String getlist() {
        return this.list;
    }

    // aggiunge un movimento alla cronologia della carta
    public void addHistoy(String newHistory) {
        this.history.add(newHistory);
    }

    // ottiene la cronologia della carta
    public ArrayList<String> getHistory() {
        return this.history;
    }
}

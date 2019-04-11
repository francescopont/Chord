package chord.network;
//attenzione: rispetto a quanto scritto lunedì ho cambiato la semantica degli attributi
public class Message {
    private final int type; //indica il tipo di messaggio (quale metodo va chiamato)
    private final boolean ack; //indica se è il messaggio è una risposta o no
    private int id; //identificativo incrementale del messaggio per tenere traccia dell'ordine dei messaggi

    //passati da costruttore e non più modificabili
    // l'id viene preso da ticket e quindi è settato in seguito

    public Message(int type, boolean ack){
        this.type=type;
        this.ack=ack;
    }

    public int getType() {
        return type;
    }

    public boolean isAck() {
        return ack;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


}

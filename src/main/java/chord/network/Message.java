package chord.network;

public class Message {
    private final int type; //indica il tipo di messaggio (quale metodo va chiamato)
    private final boolean ack; //indica se è richiesto un ack o meno (succhia fra)
    private final int id; //identificativo del messaggio per tenere traccia dell'ordine dei messaggi (incrementale non lo usano dal 1800)

    //passati da costruttore e non più modificabili

    public Message(int type, boolean ack, int id){
        this.type=type;
        this.ack=ack;
        this.id=id;
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


}

package chord.Messages;


import chord.Exceptions.TimerExpiredException;
import chord.model.NodeInfo;

import java.io.Serializable;



public class  Message implements Serializable{
    private final NodeInfo destination; //the destination address of this message
    private final int type; //indica il tipo di messaggio (quale metodo va chiamato)
    private final boolean ack; //indica se è il messaggio richiede una risposta
    protected int id; //identificativo incrementale del messaggio per tenere traccia dell'ordine dei messaggi
    protected TimerExpiredException exception;
    private final NodeInfo sender;

    //constructor
    public Message(int type, boolean ack, NodeInfo destination, NodeInfo sender){
        this.type=type;
        this.ack=ack;
        this.destination = destination;
        this.sender=sender;

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

    public NodeInfo getDestination() {
        return destination;
    }

    public NodeInfo getSender(){return sender;}

    public TimerExpiredException getException() {
        return exception;
    }

    public void setException(TimerExpiredException exception) {
        this.exception = exception;
    }

    //to check if the current message has an exception
    public void check() throws TimerExpiredException {
        if (this.exception != null){
            throw  this.exception;
        }
    }

}

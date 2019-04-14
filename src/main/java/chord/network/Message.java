package chord.network;


import chord.model.NodeInfo;

import java.io.Serializable;


public class Message implements Serializable{

    private final NodeInfo nodeInfo; //the destination address of this message
    private final int type; //indica il tipo di messaggio (quale metodo va chiamato)
    private final boolean ack; //indica se è il messaggio richiede una risposta
    private int id; //identificativo incrementale del messaggio per tenere traccia dell'ordine dei messaggi

    //passati da costruttore e non più modificabili
    // l'id viene preso da ticket e quindi è settato in seguito

    public Message(int type, boolean ack, NodeInfo nodeInfo){
        this.type=type;
        this.ack=ack;
        this.nodeInfo = nodeInfo;
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

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }


}

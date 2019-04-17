package chord.Messages;

import chord.model.NodeInfo;

public class PingAnswerMessage extends Message {
    public PingAnswerMessage(NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination,sender) ;
        this.id=ticket;
    }

}

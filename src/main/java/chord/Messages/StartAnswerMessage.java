package chord.Messages;

import chord.model.NodeInfo;

public class StartAnswerMessage extends Message {
    public StartAnswerMessage(NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination,sender) ;
        this.id=ticket;
    }
}

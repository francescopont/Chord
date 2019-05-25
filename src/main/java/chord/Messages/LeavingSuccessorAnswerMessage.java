package chord.Messages;

import chord.model.NodeInfo;

public class LeavingSuccessorAnswerMessage extends Message {
    public LeavingSuccessorAnswerMessage( NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination, sender);
        this.id=ticket;
    }
}

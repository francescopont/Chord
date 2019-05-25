package chord.Messages;

import chord.model.NodeInfo;

public class LeavingPredecessorAnswerMessage extends Message {
    public LeavingPredecessorAnswerMessage(NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination, sender);
        this.id = ticket;
    }
}

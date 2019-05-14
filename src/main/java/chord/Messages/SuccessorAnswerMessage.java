package chord.Messages;

import chord.model.NodeInfo;

public class SuccessorAnswerMessage extends Message {
    private final NodeInfo successor;

    public SuccessorAnswerMessage(NodeInfo destination, NodeInfo successor, NodeInfo sender, int ticket ) {
        super(6, false, destination,sender);
        this.successor = successor;
        this.id=ticket;
    }

    public NodeInfo getSuccessor() {
        return successor;
    }




}

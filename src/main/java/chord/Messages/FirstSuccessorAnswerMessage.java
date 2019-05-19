package chord.Messages;

import chord.model.NodeInfo;

public class FirstSuccessorAnswerMessage extends Message{
    private final NodeInfo successor;

    public FirstSuccessorAnswerMessage(NodeInfo destination, NodeInfo successor, NodeInfo sender, int ticket ) {
        super(6, false, destination,sender);
        this.successor = successor;
        this.id=ticket;
    }

    public NodeInfo getSuccessor() {
        return successor;
    }
}

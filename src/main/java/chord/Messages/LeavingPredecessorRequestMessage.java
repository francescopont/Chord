package chord.Messages;

import chord.model.NodeInfo;

public class LeavingPredecessorRequestMessage extends Message {
    private final NodeInfo newPredecessor;

    public LeavingPredecessorRequestMessage(NodeInfo destination, NodeInfo newPredecessor, NodeInfo sender) {
        super(44, true, destination, sender);
        this.newPredecessor = newPredecessor;
    }

    public NodeInfo getNewPredecessor() {
        return newPredecessor;
    }

}

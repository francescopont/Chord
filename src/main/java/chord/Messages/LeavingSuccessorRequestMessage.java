package chord.Messages;

import chord.model.NodeInfo;

public class LeavingSuccessorRequestMessage extends Message {
    private final NodeInfo newSuccessor;
    public LeavingSuccessorRequestMessage( NodeInfo destination, NodeInfo newSuccessor, NodeInfo sender) {
        super(45, true, destination, sender);
        this.newSuccessor = newSuccessor;
    }
    public NodeInfo getNewSuccessor() {
        return newSuccessor;
    }

}

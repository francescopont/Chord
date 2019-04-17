package chord.Messages;

import chord.Messages.Message;
import chord.model.NodeInfo;

public class SuccessorAnswerMessage extends Message {
    private final NodeInfo successor;

    public SuccessorAnswerMessage(NodeInfo nodeInfo, NodeInfo successor ) {
        super(6, false, nodeInfo);
        this.successor = successor;
    }

    public NodeInfo getSuccessor() {
        return successor;
    }


}

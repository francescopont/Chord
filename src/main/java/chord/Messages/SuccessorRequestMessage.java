package chord.Messages;

import chord.model.NodeInfo;

public class SuccessorRequestMessage extends Message {
    //I'm asking for the successor of node identifier
    String nodeidentifier;
    public SuccessorRequestMessage( NodeInfo destination, String nodeidentifier) {
        super(3, true, destination);
        this.nodeidentifier = nodeidentifier;

    }
}

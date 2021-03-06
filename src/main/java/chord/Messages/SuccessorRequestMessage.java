package chord.Messages;

import chord.model.NodeInfo;

public class SuccessorRequestMessage extends Message {


    //I'm asking for the successor of node identifier
    private final String   nodeidentifier;
    public SuccessorRequestMessage( NodeInfo destination, String nodeidentifier, NodeInfo sender) {
        super(3, true, destination, sender);
        this.nodeidentifier = nodeidentifier;
    }

    public String getNodeidentifier() {
        return nodeidentifier;
    }
}

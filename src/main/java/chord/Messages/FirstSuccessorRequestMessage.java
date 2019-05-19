package chord.Messages;

import chord.model.NodeInfo;

public class FirstSuccessorRequestMessage extends Message {

    //I'm asking for the first successor of the destination
    public FirstSuccessorRequestMessage(NodeInfo destination, NodeInfo sender) {
        super(5, true, destination, sender);
    }

}

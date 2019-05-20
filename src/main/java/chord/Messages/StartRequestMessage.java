package chord.Messages;

import chord.model.NodeInfo;

public class StartRequestMessage extends Message{

    public StartRequestMessage(NodeInfo destination, NodeInfo sender) {

        super(33, true, destination,sender);
    }
}

package chord.Messages;

import chord.model.NodeInfo;

public class PingRequestMessage extends Message {

    public PingRequestMessage(NodeInfo destination, NodeInfo sender) {

        super(1, true, destination,sender);
    }


}

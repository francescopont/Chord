package chord.Messages;

import chord.model.Node;
import chord.model.NodeInfo;

public class PredecessorRequestMessage extends Message {


    public PredecessorRequestMessage(NodeInfo destination,  NodeInfo sender) {
        super(2, true, destination, sender);
    }


}

package chord.Messages;

import chord.model.NodeInfo;

public class NotifyRequestMessage extends Message {
    public NotifyRequestMessage( NodeInfo destination, NodeInfo sender) {
        super(4 , true, destination, sender);
    }
}

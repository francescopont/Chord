package chord.Messages;

import chord.model.NodeInfo;

public class NotifyAnswerMessage extends Message {
    public NotifyAnswerMessage(int type, boolean ack, NodeInfo destination, NodeInfo sender) {
        super(6, false, destination, sender);
    }
}

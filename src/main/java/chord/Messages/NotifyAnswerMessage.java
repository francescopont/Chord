package chord.Messages;

import chord.model.NodeInfo;

public class NotifyAnswerMessage extends Message {
    public NotifyAnswerMessage( NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination, sender);
        this.id=ticket;
    }
}

package chord.Messages;

import chord.model.NodeInfo;

public class DeleteFileAnswerMessage extends Message {

    public DeleteFileAnswerMessage(NodeInfo destination, NodeInfo sender, int ticket) {
        super(6, false, destination, sender);
        this.id=ticket;
    }

}

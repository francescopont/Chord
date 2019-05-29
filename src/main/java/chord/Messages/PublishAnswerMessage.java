package chord.Messages;

import chord.model.NodeInfo;

public class PublishAnswerMessage extends Message {

    public PublishAnswerMessage(NodeInfo destination, NodeInfo sender, int ticket){
        super(6,false,destination,sender);
        this.id=ticket;
    }
}

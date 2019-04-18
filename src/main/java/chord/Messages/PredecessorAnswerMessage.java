package chord.Messages;

import chord.model.NodeInfo;

public class PredecessorAnswerMessage extends Message {
    private final NodeInfo predecessor;


    public PredecessorAnswerMessage(NodeInfo destination, NodeInfo predecessor, NodeInfo sender, int ticket ) {
        super(6, false, destination, sender);
        this.id = ticket;
        this.predecessor = predecessor;
    }

    public NodeInfo getPredecessor() {
        return predecessor;
    }


}

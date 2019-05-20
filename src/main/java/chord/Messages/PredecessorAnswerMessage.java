package chord.Messages;

import chord.Exceptions.PredecessorException;
import chord.model.NodeInfo;

public class PredecessorAnswerMessage extends Message {
    private final NodeInfo predecessor;
    private PredecessorException predecessorException;


    public PredecessorAnswerMessage(NodeInfo destination, NodeInfo predecessor, NodeInfo sender, int ticket, boolean exception ) {
        super(6, false, destination, sender);
        this.id = ticket;
        this.predecessor = predecessor;
        if (exception){
            this.predecessorException = new PredecessorException();
        }
        else {
            this.predecessorException = null;
        }
    }

    public NodeInfo getPredecessor() {
        return predecessor;
    }

    public void checkPredecessorException() throws PredecessorException{
        if (this.predecessorException != null){
            throw this.predecessorException;
        }
    }




}

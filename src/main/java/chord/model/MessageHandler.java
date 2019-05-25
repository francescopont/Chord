package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Messages.*;
import chord.network.Router;

public class MessageHandler implements Runnable {
    private Message message;
    private Node node;

    //constructor
    public MessageHandler (Node node, Message message){
        this.node = node;
        this.message=message;
    }

    @Override
    public void run() {
        switch (message.getType()){
            case 1://ping
                PingAnswerMessage pingAnswerMessage= new PingAnswerMessage(message.getSender(),message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),pingAnswerMessage);
                break;
            case 2: //predecessor
                boolean exception = false;
                NodeInfo predecessor = null;
                try{
                    predecessor = node.getPredecessor();
                }catch (PredecessorException e){
                    exception = true;
                }
                PredecessorAnswerMessage predecessorAnswerMessage = new PredecessorAnswerMessage(message.getSender(), predecessor,message.getDestination(),message.getId(), exception);
                Router.sendAnswer(node.getPort(), predecessorAnswerMessage);
                break;

            case 3: //find successor
                NodeInfo successor=node.findSuccessor(((SuccessorRequestMessage) message).getNodeidentifier());
                SuccessorAnswerMessage successorAnswerMessage= new SuccessorAnswerMessage(message.getSender(),successor,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),successorAnswerMessage);
                break;

            case 4:  //notify
                node.notify(message.getSender());
                NotifyAnswerMessage notifyAnswerMessage= new NotifyAnswerMessage(message.getSender(),message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(),notifyAnswerMessage);
                break;

            case 5:  // first successor
                NodeInfo firstSuccessor= node.getFirstSuccessor();
                FirstSuccessorAnswerMessage firstSuccessorAnswerMessage= new FirstSuccessorAnswerMessage(message.getSender(),firstSuccessor,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),firstSuccessorAnswerMessage);
                break;

            case 6: //answer message
                NodeDispatcher dispatcher = node.getDispatcher();
                dispatcher.addAnswer(message.getId(),message);
                break;


            case 33: //start message
                node.start(message.getSender());
                StartAnswerMessage startAnswerMessage = new StartAnswerMessage(message.getSender(), message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), startAnswerMessage);
                break;
            case 44: // leavePredecessor message
                node.notifyLeavingPredecessor( ((LeavingPredecessorRequestMessage) message).getNewPredecessor());
                LeavingPredecessorAnswerMessage leavingPredecessorAnswerMessage = new LeavingPredecessorAnswerMessage(message.getSender(), message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(), leavingPredecessorAnswerMessage);
                break;
            case 45: // leaveSuccessor message
                node.notifyLeavingSuccessor(((LeavingSuccessorRequestMessage) message).getNewSuccessor());
                LeavingSuccessorAnswerMessage leavingSuccessorAnswerMessage = new LeavingSuccessorAnswerMessage(message.getSender(), message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(), leavingSuccessorAnswerMessage);
        }
    }
}


//protocol of types
/*
    ping 1
    request of predecessor 2
    find successor 3
    notify 4
    first successor 5

    start 33
    leavingPredecessor 44
    leavingSuccessor 45

    reply 6
 */


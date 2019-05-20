package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Messages.*;
import chord.network.Router;

//CODE MODIFIED FOR TESTING
public class MessageHandler implements Runnable {
    private Message message;
    private Node node;

    public MessageHandler (Node node, Message message){
        this.node = node;
        this.message=message;
    }

    @Override
    public void run() {
        //System.out.println("Message: "+ message.getId() + "; destination: "+ message.getDestination().getHash() + "; sender: "+ message.getSender().getHash()+ "; type: "+ message.getType());
        switch (message.getType()){
            case 1:
                //ping
                PingAnswerMessage pingAnswerMessage= new PingAnswerMessage(message.getSender(),message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),pingAnswerMessage);
                break;
            case 2:
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

            case 3:
                NodeInfo successor=node.find_successor(((SuccessorRequestMessage) message).getNodeidentifier());
                SuccessorAnswerMessage successorAnswerMessage= new SuccessorAnswerMessage(message.getSender(),successor,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),successorAnswerMessage);
                break;

            case 4:
                node.notify(message.getSender());
                NotifyAnswerMessage notifyAnswerMessage= new NotifyAnswerMessage(message.getSender(),message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(),notifyAnswerMessage);
                break;

            case 5:
                NodeInfo firstSuccessor= node.getFirstSuccessor();
                FirstSuccessorAnswerMessage firstSuccessorAnswerMessage= new FirstSuccessorAnswerMessage(message.getSender(),firstSuccessor,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),firstSuccessorAnswerMessage);
                break;

            case 6:
                NodeDispatcher dispatcher = node.getDispatcher();
                dispatcher.addAnswer(message.getId(),message);
                break;
            case 33:
                node.start(message.getSender());
                StartAnswerMessage startAnswerMessage = new StartAnswerMessage(message.getSender(), message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), startAnswerMessage);
        }
    }
}


//please put protocol of types here!!!
/*
    ping 1
    request of predecessor 2
    find successor 3
    notify 4
    first successor 5

    start 33

    reply 6
 */


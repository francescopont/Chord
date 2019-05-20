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
        switch (message.getType()){
            case 1:
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
                //chi riceve la notify controlla se chi gliel'ha mandata possa essere il suo predecessore o meno giusto?
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



    reply 6
 */


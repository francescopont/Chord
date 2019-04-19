package chord.model;

import chord.Messages.*;
import chord.network.Router;

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
                Router.sendMessage(node.getPort(),notifyAnswerMessage);
                break;
            case 6:
                NodeDispatcher dispatcher = node.getDispatcher();
                dispatcher.addAnswer(message.getId(),message);
                dispatcher.notifyAll();
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
     initial request of successor 6

    ack 0
    reply 6
 */


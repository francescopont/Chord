package chord.model;

import chord.Messages.Message;
import chord.Messages.PingAnswerMessage;
import chord.Messages.SuccessorAnswerMessage;
import chord.Messages.SuccessorRequestMessage;
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
            case 6:
                node.addAnswer(message.getId(),message);
                node.notifyAll();
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


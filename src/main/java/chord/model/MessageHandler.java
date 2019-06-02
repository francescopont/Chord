package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Messages.*;
import chord.network.Router;

import java.util.Map;

/**
 * Class to handle a message when it is delivered to a node and send the answer
 *
 * Message protocol:
 * #1: Ping message
 * #2: Request of predecessor message
 * #3: Find successor message
 * #4: Notify message
 * #5: First successor request message
 * #6: Reply message
 * #17: Delete file message
 * #25: Lookup message
 * #33: Start message
 * #44: Leaving predecessor message
 * #45: Leaving successor message
 * #85: Publish message
 *
 */
public class MessageHandler implements Runnable {
    private Message message;
    private Node node;

    public MessageHandler (Node node, Message message){
        this.node = node;
        this.message=message;
    }

    @Override
    public void run() {
        if (node.isStarted()){
            node.start(message.getSender());
        }
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
                Map<String, String> newFiles = node.getFileSystem().retrieveFiles(message.getSender().getHash());
                NotifyAnswerMessage notifyAnswerMessage= new NotifyAnswerMessage(message.getSender(),message.getDestination(), newFiles, message.getId());
                Router.sendAnswer(node.getPort(),notifyAnswerMessage);
                break;

            case 5:  // first successor
                NodeInfo firstSuccessor= node.getFirstSuccessor();
                FirstSuccessorAnswerMessage firstSuccessorAnswerMessage= new FirstSuccessorAnswerMessage(message.getSender(),firstSuccessor,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(),firstSuccessorAnswerMessage);
                break;

            case 6: //answer
                NodeDispatcher dispatcher = node.getDispatcher();
                dispatcher.addAnswer(message.getId(),message);
                break;

            case 17: // delete
                node.deleteMyFile(((DeleteFileRequestMessage) message).getKey());
                DeleteFileAnswerMessage deleteFileAnswerMessage= new DeleteFileAnswerMessage(message.getSender(), message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), deleteFileAnswerMessage);
                break;

            case 25: // file request
                String file = node.getMyFile(((FileRequestMessage) message).getKey());
                FileAnswerMessage fileAnswerMessage= new FileAnswerMessage(message.getSender(),file,message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), fileAnswerMessage);
                break;

            case 33: //start
                node.start(message.getSender());
                StartAnswerMessage startAnswerMessage = new StartAnswerMessage(message.getSender(), message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), startAnswerMessage);
                break;

            case 44: // leavePredecessor
                node.notifyLeavingPredecessor( ((LeavingPredecessorRequestMessage) message).getNewPredecessor());
                if (!((LeavingPredecessorRequestMessage) message).getFiles().isEmpty()){
                    for (Map.Entry<String, String> files : ((LeavingPredecessorRequestMessage) message).getFiles().entrySet()){
                        node.publishFile(files.getKey(), files.getValue());
                    }
                }
                LeavingPredecessorAnswerMessage leavingPredecessorAnswerMessage = new LeavingPredecessorAnswerMessage(message.getSender(), message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(), leavingPredecessorAnswerMessage);
                break;
            case 45: // leaveSuccessor
                node.notifyLeavingSuccessor(((LeavingSuccessorRequestMessage) message).getNewSuccessor());
                LeavingSuccessorAnswerMessage leavingSuccessorAnswerMessage = new LeavingSuccessorAnswerMessage(message.getSender(), message.getDestination(), message.getId());
                Router.sendAnswer(node.getPort(), leavingSuccessorAnswerMessage);
                break;

            case 85: // publish
                node.publishFile(((PublishRequestMessage) message).getKey(), ((PublishRequestMessage) message).getData());
                PublishAnswerMessage publishAnswerMessage= new PublishAnswerMessage(message.getSender(), message.getDestination(),message.getId());
                Router.sendAnswer(node.getPort(), publishAnswerMessage);
                break;


        }
    }
}



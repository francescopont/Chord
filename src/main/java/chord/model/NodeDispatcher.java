package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Exceptions.TimerExpiredException;
import chord.Messages.*;
import chord.network.Router;

import java.util.*;

/**
 * Class that manages the communication of the node to which it is associated with the other nodes
 * Creates and forwards at the lower layers the different types of messages
 */
public class NodeDispatcher {

    /**
     * The port associated to this node
     */
    private int port;

    /**
     * List of tickets associated with messages that are waiting for an answer
     */
    private List<Integer> waitingTickets;

    /**
     * List of answers received
     */
    private HashMap<Integer, Message> answers;

    public NodeDispatcher(int port){
        this.answers = new HashMap<>();
        this.port = port;
        this.waitingTickets = new LinkedList<>();
    }

    /**
     *Set of methods used to send a message and handle the answer
     */

    /**
     * Create and forward a message of notify
     * @param destination information of the message destination
     * @param sender information of the message sender
     * @return
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized Map<String, String> sendNotify(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException {
        NotifyRequestMessage notifyRequestMessage=new NotifyRequestMessage(destination, sender);
        final int ticket= Router.sendMessage(this.port,notifyRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run(){
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        NotifyAnswerMessage notifyAnswerMessage = new NotifyAnswerMessage(sender, destination,new HashMap<>(), ticket);
                        notifyAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, notifyAnswerMessage);
                    }
                }
            }
        });
        while (!this.answers.containsKey(ticket)) {
            try {
                wait();
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
        NotifyAnswerMessage notifyAnswerMessage = (NotifyAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        notifyAnswerMessage.check();
        return notifyAnswerMessage.getFiles();
    }

    /**
     * Create and send a message to request the predecessor of the destination of the message
     * @param destination information of the message destination
     * @param sender information of the message sender
     * @return the predecessor of the node, if it exists
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     * @throws PredecessorException Exception thrown if the predecessor of the receiver of the message does not exist
     */
    public  synchronized NodeInfo sendPredecessorRequest(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException, PredecessorException {
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(destination, sender);
        final int ticket = Router.sendMessage(this.port, predecessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        PredecessorAnswerMessage predecessorAnswerMessage = new PredecessorAnswerMessage(sender,null, destination,ticket, false);
                        predecessorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket,predecessorAnswerMessage);
                    }
                }

            }
        });
        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
            }
        }
        PredecessorAnswerMessage answerMessage = (PredecessorAnswerMessage) this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        answerMessage.check();
        answerMessage.checkPredecessorException();
        return answerMessage.getPredecessor();
    }

    /**
     * Create and send a request to know the successor of a given key
     * @param destination information of the message destination
     * @param node key of the node whose successor the sender is looking for
     * @param sender information of the message sender
     * @return the successor of the node if it exists
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized NodeInfo sendSuccessorRequest(final NodeInfo destination, String node, final NodeInfo sender)throws TimerExpiredException{
        SuccessorRequestMessage successorRequestMessage= new SuccessorRequestMessage(destination, node, sender);
        final int ticket= Router.sendMessage(this.port, successorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        SuccessorAnswerMessage successorAnswerMessage = new SuccessorAnswerMessage(sender,null, destination,ticket);
                        successorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket,successorAnswerMessage);
                    }
                }

            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
            }
        }
        SuccessorAnswerMessage successorAnswerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        successorAnswerMessage.check();
        return successorAnswerMessage.getSuccessor();
    }

    /**
     * Create and send a request to know the first successor of the receiver
     * @param destination information of the message destination
     * @param sender information of the message sender
     * @return the first successor of the node if it exists
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized NodeInfo sendFirstSuccessorRequest(final NodeInfo destination,final NodeInfo sender)throws TimerExpiredException{
        FirstSuccessorRequestMessage firstSuccessorRequestMessage= new FirstSuccessorRequestMessage(destination,sender);
        final int ticket= Router.sendMessage(this.port, firstSuccessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        FirstSuccessorAnswerMessage firstSuccessorAnswerMessage = new FirstSuccessorAnswerMessage(sender,null,destination,ticket);
                        firstSuccessorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket,firstSuccessorAnswerMessage);
                    }
                }

            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        FirstSuccessorAnswerMessage firstSuccessorAnswerMessage= (FirstSuccessorAnswerMessage)this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        firstSuccessorAnswerMessage.check();
        return firstSuccessorAnswerMessage.getSuccessor();
    }

    /**
     * Create and send a ping message
     * @param destination information of the message destination
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     *
     */
    public synchronized void sendPing(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException {
        PingRequestMessage pingRequestMessage = new PingRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,pingRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        PingAnswerMessage pingAnswerMessage = new PingAnswerMessage(sender,destination, ticket);
                        pingAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, pingAnswerMessage);
                    }
                }

            }
        });

        while(!this.answers.containsKey(ticket)){
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        PingAnswerMessage pingAnswerMessage = (PingAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        pingAnswerMessage.check();
    }

    /**
     * Create and send a start request message
     * @param destination information of the message destination
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized void sendStartRequest(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException{
        StartRequestMessage startRequestMessage = new StartRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,startRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        StartAnswerMessage startAnswerMessage = new StartAnswerMessage(sender,destination, ticket);
                        startAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, startAnswerMessage);
                    }
                }

            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){

            }
        }
        StartAnswerMessage startAnswerMessage = (StartAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        startAnswerMessage.check();
    }

    /**
     * Create and send a message to notify that the predecessor of the receiver is leaving Chord
     * @param destination information of the message destination
     * @param newPredecessor information about the new predecessor of the receiver (predecessor of the sender)
     * @param files files of the node that must be transfer to the successor that is now responsible for them
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized void sendLeavingPredecessorRequest(final NodeInfo destination, final NodeInfo newPredecessor, final Map<String, String> files, final NodeInfo sender) throws TimerExpiredException{
        LeavingPredecessorRequestMessage leavingPredecessorRequestMessage = new LeavingPredecessorRequestMessage(destination,newPredecessor,files, sender);
        final int ticket=Router.sendMessage(this.port, leavingPredecessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        LeavingPredecessorAnswerMessage leavingPredecessorAnswerMessage = new LeavingPredecessorAnswerMessage(sender,destination, ticket);
                        leavingPredecessorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, leavingPredecessorAnswerMessage);
                    }
                }

            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){

            }
        }
        LeavingPredecessorAnswerMessage leavingPredecessorAnswerMessage = (LeavingPredecessorAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        leavingPredecessorAnswerMessage.check();
    }

    /**
     * Create and send a message to notify that is the successor of the receiver is leaving Chord
     * @param destination information of the message destination
     * @param newSuccessor information about the new successor of the receiver (successor of the sender)
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized void sendLeavingSuccessorRequest(final NodeInfo destination, final NodeInfo newSuccessor, final NodeInfo sender) throws TimerExpiredException{
        LeavingSuccessorRequestMessage leavingSuccessorRequestMessage = new LeavingSuccessorRequestMessage(destination,newSuccessor,sender);
        final int ticket=Router.sendMessage(this.port, leavingSuccessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        LeavingSuccessorAnswerMessage leavingSuccessorAnswerMessage = new LeavingSuccessorAnswerMessage(sender,destination, ticket);
                        leavingSuccessorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, leavingSuccessorAnswerMessage);
                    }
                }

            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){

            }
        }
        LeavingSuccessorAnswerMessage leavingSuccessorAnswerMessage = (LeavingSuccessorAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        leavingSuccessorAnswerMessage.check();
    }

    /**
     * Create and send a request of publish message
     * @param destination information of the message destination (node responsible of the key of the file)
     * @param data file to publish
     * @param key of the file
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized void sendPublishRequest(final NodeInfo destination, final String data, final String key, final NodeInfo sender) throws TimerExpiredException{
        PublishRequestMessage publishRequestMessage= new PublishRequestMessage(destination,data,key,sender);
        final int ticket=Router.sendMessage(this.port, publishRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: leavingSuccessor "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
                        PublishAnswerMessage publishAnswerMessage = new PublishAnswerMessage(sender,destination,ticket);
                        publishAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, publishAnswerMessage);
                    }
                }
            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        PublishAnswerMessage publishAnswerMessage = (PublishAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        publishAnswerMessage.check();
    }

    /**
     * Create and send a request for a file with an associated key
     * @param destination information of the message destination (node responsible of the key of the file)
     * @param key of the file the sender is looking for
     * @param sender information of the message sender
     * @return the file associated to the key
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized String sendFileRequest(final NodeInfo destination,final String key, final NodeInfo sender) throws TimerExpiredException{
        FileRequestMessage fileRequestMessage= new FileRequestMessage(destination,key,sender);
        final int ticket=Router.sendMessage(this.port, fileRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: leavingSuccessor "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
                        FileAnswerMessage fileAnswerMessage= new FileAnswerMessage(sender,null,destination,ticket);
                        fileAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, fileAnswerMessage);
                    }
                }
            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        FileAnswerMessage fileAnswerMessage = (FileAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        fileAnswerMessage.check();
        return fileAnswerMessage.getFile();
    }

    /**
     * Create and send a request for delete the file associated to the passed key
     * @param destination information of the message destination (node responsible of the key of the file)
     * @param key of the file to delete
     * @param sender information of the message sender
     * @throws TimerExpiredException Exception thrown if the answer does not arrive within the expiration of the timer
     */
    public synchronized void sendDeleteFileRequest(final NodeInfo destination, final String key, final NodeInfo sender) throws TimerExpiredException{
        DeleteFileRequestMessage deleteFileRequestMessage= new DeleteFileRequestMessage(destination,key,sender);
        final int ticket=Router.sendMessage(this.port, deleteFileRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: leavingSuccessor "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
                        DeleteFileAnswerMessage deleteFileAnswerMessage= new DeleteFileAnswerMessage(sender,destination,ticket);
                        deleteFileAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, deleteFileAnswerMessage);
                    }
                }
            }
        });
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        DeleteFileAnswerMessage deleteFileAnswerMessage = (DeleteFileAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        deleteFileAnswerMessage.check();
    }

    /**
     * Receive and add an answer to the list
     * @param ticket of the message that waits a reply (same ticket for request and answer)
     * @param message of reply
     */
    public synchronized void addAnswer(int ticket, Message message){
        if( waitingTickets.contains(ticket) && !answers.containsKey(ticket)){
            answers.put(ticket,message);
            notifyAll();
        }

    }
}

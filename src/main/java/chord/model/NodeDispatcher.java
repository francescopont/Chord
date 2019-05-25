package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Exceptions.TimerExpiredException;
import chord.Messages.*;
import chord.network.Router;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TimerTask;

public class NodeDispatcher {

    //the port associated to this node
    private int port;

    // the tickets that are waiting for an answer
    private List<Integer> waitingTickets;

    //the answers
    private HashMap<Integer, Message> answers;

    //constrcutor
    public NodeDispatcher(int port){
        this.answers = new HashMap<>();
        this.port = port;
        this.waitingTickets = new LinkedList<>();
    }

    //a set of methods used to send a message and handle the answer
    public synchronized void sendNotify(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException {
        NotifyRequestMessage notifyRequestMessage=new NotifyRequestMessage(destination, sender);
        final int ticket= Router.sendMessage(this.port,notifyRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run(){
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: notify "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
                        NotifyAnswerMessage notifyAnswerMessage = new NotifyAnswerMessage(sender, destination,ticket);
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
                e.printStackTrace();
            }
        }
        NotifyAnswerMessage notifyAnswerMessage = (NotifyAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        notifyAnswerMessage.check();
    }

    public  synchronized NodeInfo sendPredecessorRequest(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException, PredecessorException {
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(destination, sender);
        final int ticket = Router.sendMessage(this.port, predecessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: sendPredecessorRequest "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
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
                e.printStackTrace();
            }
        }
        PredecessorAnswerMessage answerMessage = (PredecessorAnswerMessage) this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        answerMessage.check();
        answerMessage.checkPredecessorException();
        return answerMessage.getPredecessor();
    }


    public synchronized NodeInfo sendSuccessorRequest(final NodeInfo destination, String node, final NodeInfo sender)throws TimerExpiredException{
        SuccessorRequestMessage successorRequestMessage= new SuccessorRequestMessage(destination, node, sender);
        final int ticket= Router.sendMessage(this.port, successorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: sendSuccessroRequest "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );
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
                e.printStackTrace();
            }
        }
        SuccessorAnswerMessage successorAnswerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        successorAnswerMessage.check();
        return successorAnswerMessage.getSuccessor();
    }

    public synchronized NodeInfo sendFirstSuccessorRequest(final NodeInfo destination,final NodeInfo sender)throws TimerExpiredException{
        FirstSuccessorRequestMessage firstSuccessorRequestMessage= new FirstSuccessorRequestMessage(destination,sender);
        final int ticket= Router.sendMessage(this.port, firstSuccessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new Runnable() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: sendFirstSuccessorRequest "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );

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
                e.printStackTrace();
            }
        }
        FirstSuccessorAnswerMessage firstSuccessorAnswerMessage= (FirstSuccessorAnswerMessage)this.answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        firstSuccessorAnswerMessage.check();
        return firstSuccessorAnswerMessage.getSuccessor();
    }

    public synchronized void sendPing(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException{
        PingRequestMessage pingRequestMessage = new PingRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,pingRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: ping "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );

                        PingAnswerMessage pingAnswerMessage = new PingAnswerMessage(sender,destination, ticket);
                        pingAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, pingAnswerMessage);
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
        PingAnswerMessage pingAnswerMessage = (PingAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        pingAnswerMessage.check();
    }


    public synchronized void sendStartRequest(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException{
        StartRequestMessage startRequestMessage = new StartRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,startRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: sendStartRequest "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );

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
                e.printStackTrace();
            }
        }
        StartAnswerMessage startAnswerMessage = (StartAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        startAnswerMessage.check();
    }

    public synchronized void sendLeavingPredecessorRequest(final NodeInfo destination, final NodeInfo newPredecessor, final NodeInfo sender) throws TimerExpiredException{
        LeavingPredecessorRequestMessage leavingPredecessorRequestMessage = new LeavingPredecessorRequestMessage(destination,newPredecessor,sender);
        final int ticket=Router.sendMessage(this.port, leavingPredecessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: leavingPredecessor "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );

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
                e.printStackTrace();
            }
        }
        LeavingPredecessorAnswerMessage leavingPredecessorAnswerMessage = (LeavingPredecessorAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        leavingPredecessorAnswerMessage.check();
    }

    public synchronized void sendLeavingSuccessorRequest(final NodeInfo destination, final NodeInfo newSuccessor, final NodeInfo sender) throws TimerExpiredException{
        LeavingSuccessorRequestMessage leavingSuccessorRequestMessage = new LeavingSuccessorRequestMessage(destination,newSuccessor,sender);
        final int ticket=Router.sendMessage(this.port, leavingSuccessorRequestMessage);
        this.waitingTickets.add(ticket);
        Threads.executeAfterDelay(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        System.out.println("method: leavingSuccessor "+ "id: "+ ticket+ " sender: "+ sender.getHash() + " destination: "+ destination.getHash() );

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
                e.printStackTrace();
            }
        }
        LeavingSuccessorAnswerMessage leavingSuccessorAnswerMessage = (LeavingSuccessorAnswerMessage) answers.get(ticket);
        waitingTickets.remove((Integer) ticket);
        answers.remove(ticket);
        leavingSuccessorAnswerMessage.check();
    }

    //this method is used when an answer is received
    public synchronized void addAnswer(int ticket, Message message){
        if( waitingTickets.contains(ticket) && !answers.containsKey(ticket)){
            answers.put(ticket,message);
            notifyAll();
        }

    }
}

package chord.model;

import chord.Exceptions.PredecessorException;
import chord.Exceptions.TimerExpiredException;
import chord.Messages.*;
import chord.network.Router;

import java.util.*;

//problema: la sincronizzazione

public class NodeDispatcher {
    private HashMap<Integer, Message> answers;
    private int port;
    private List<Integer> waitingTickets;

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
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run(){
                synchronized (this){
                    if(waitingTickets.contains(ticket)){
                        NotifyAnswerMessage notifyAnswerMessage = new NotifyAnswerMessage(sender, destination,ticket);
                        notifyAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, notifyAnswerMessage);
                    }
                }
            }
        }, Utilities.getTimer());

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

    public synchronized NodeInfo sendPredecessorRequest(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException, PredecessorException {
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(destination, sender);
        final int ticket = Router.sendMessage(this.port, predecessorRequestMessage);
        this.waitingTickets.add(ticket);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
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
        }, Utilities.getTimer());

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
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
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
        }, Utilities.getTimer());
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
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
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
        }, Utilities.getTimer());
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
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
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
        }, Utilities.getTimer());
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
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
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
        }, Utilities.getTimer());
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

    //this method is used when an answer is received
    public synchronized void addAnswer(int ticket, Message message){
        if( waitingTickets.contains(ticket) && !answers.containsKey(ticket)){
            answers.put(ticket,message);
            notifyAll();
        }

    }
}

package chord.model;

import chord.Exceptions.TimerExpiredException;
import chord.Messages.*;
import chord.network.Router;

//l'unica cosa a carico di node è eliminare il messaggio nl caso in cui contenga un errore ( conosce il ticket a partire dal messaggio di errore)
//problema: la sincronizzazione
import java.util.*;

public class NodeDispatcher {
    private HashMap<Integer, Message> answers;
    private int port;
    private List<Integer> waiting_tickets;

    public NodeDispatcher(int port){
        this.answers = new HashMap<>();
        this.port = port;
        this.waiting_tickets = new LinkedList<>();
    }

    public synchronized void sendNotify(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException {
        NotifyRequestMessage notifyRequestMessage=new NotifyRequestMessage(destination, sender);
        final int ticket= Router.sendMessage(this.port,notifyRequestMessage);
        this.waiting_tickets.add(ticket);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run(){
                synchronized (this){
                    if(waiting_tickets.contains(ticket)){
                        NotifyAnswerMessage notifyAnswerMessage = new NotifyAnswerMessage(sender, destination,ticket);
                        notifyAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, notifyAnswerMessage);
                    }
                }
            }
        }, 10000);

        while (!this.answers.containsKey(ticket)) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        NotifyAnswerMessage notifyAnswerMessage = (NotifyAnswerMessage) answers.get(ticket);
        waiting_tickets.remove((Integer) ticket);
        answers.remove(ticket);
        notifyAnswerMessage.check();
    }

    public synchronized NodeInfo sendPredecessorRequest(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException{
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(destination, sender);
        final int ticket = Router.sendMessage(this.port, predecessorRequestMessage);
        this.waiting_tickets.add(ticket);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waiting_tickets.contains(ticket)){
                        PredecessorAnswerMessage predecessorAnswerMessage = new PredecessorAnswerMessage(sender,null, destination,ticket);
                        predecessorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket,predecessorAnswerMessage);
                    }
                }

            }
        }, 10000);

        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
                e.printStackTrace();
            }
        }
        PredecessorAnswerMessage answerMessage = (PredecessorAnswerMessage) this.answers.get(ticket);
        waiting_tickets.remove((Integer) ticket);
        answers.remove(ticket);
        answerMessage.check();
        return answerMessage.getPredecessor();
    }


    public synchronized NodeInfo sendSuccessorRequest(final NodeInfo destination, String node, final NodeInfo sender)throws TimerExpiredException{
        SuccessorRequestMessage successorRequestMessage= new SuccessorRequestMessage(destination, node, sender);
        final int ticket= Router.sendMessage(this.port, successorRequestMessage);
        this.waiting_tickets.add(ticket);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waiting_tickets.contains(ticket)){
                        SuccessorAnswerMessage successorAnswerMessage = new SuccessorAnswerMessage(sender,null, destination,ticket);
                        successorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket,successorAnswerMessage);
                    }
                }

            }
        }, 10000);
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        SuccessorAnswerMessage successorAnswerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        waiting_tickets.remove((Integer) ticket);
        answers.remove(ticket);
        successorAnswerMessage.check();
        return successorAnswerMessage.getSuccessor();
    }

    public synchronized void sendPing(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException{
        PingRequestMessage pingRequestMessage = new PingRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,pingRequestMessage);
        this.waiting_tickets.add(ticket);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this){
                    if(waiting_tickets.contains(ticket)){
                        PingAnswerMessage successorAnswerMessage = new PingAnswerMessage(sender,destination, ticket);
                        successorAnswerMessage.setException(new TimerExpiredException());
                        addAnswer(ticket, successorAnswerMessage);
                    }
                }

            }
        }, 10000);
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        PingAnswerMessage pingAnswerMessage = (PingAnswerMessage) answers.get(ticket);
        waiting_tickets.remove((Integer) ticket);
        answers.remove(ticket);
        pingAnswerMessage.check();
        System.out.println("ping terminato correttamente da: "+ this.port);
    }

    public synchronized void addAnswer(int ticket, Message message){
        if( waiting_tickets.contains(ticket) && !answers.containsKey(ticket)){
            answers.put(ticket,message);
            notifyAll();
        }

    }
}

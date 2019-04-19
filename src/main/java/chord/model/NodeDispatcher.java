package chord.model;

import chord.Messages.*;
import chord.network.Router;

//l'unica cosa a carico di node è eliminare il messaggio nl caso in cui contenga un errore ( conosce il ticket a partire dal messaggio di errore)

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

public class NodeDispatcher {
    private Hashtable<Integer, Message> answers;
    private int port;
    private NodeInfo nodeInfo;

    public NodeDispatcher(int port, NodeInfo nodeInfo){
        this.answers = new Hashtable<>();
        this.port = port;
        this.nodeInfo = nodeInfo;
    }

    public void sendNotify(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException{
        NotifyRequestMessage notifyRequestMessage=new NotifyRequestMessage(destination, sender);
        final int ticket= Router.sendMessage(this.port,notifyRequestMessage);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!answers.containsKey(ticket)){
                    NotifyAnswerMessage notifyAnswerMessage = new NotifyAnswerMessage(sender, destination,ticket);
                    notifyAnswerMessage.setException(new TimerExpiredException(ticket));
                    answers.put(ticket,notifyAnswerMessage);
                    notifyAll();
                }
                else{
                    answers.remove(ticket);
                }
            }
        }, 1000);


        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
                e.printStackTrace();
            }
        }

        NotifyAnswerMessage notifyAnswerMessage = (NotifyAnswerMessage) answers.get(ticket);
        notifyAnswerMessage.check();
    }

    public NodeInfo sendPredecessorRequest(final NodeInfo destination, final NodeInfo sender)throws TimerExpiredException{
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(destination, sender);
        final int ticket = Router.sendMessage(this.port, predecessorRequestMessage);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!answers.containsKey(ticket)){
                    PredecessorAnswerMessage predecessorAnswerMessage = new PredecessorAnswerMessage(sender,null, destination,ticket);
                    predecessorAnswerMessage.setException(new TimerExpiredException(ticket));
                    answers.put(ticket,predecessorAnswerMessage);
                    notifyAll();
                }
                else{
                    answers.remove(ticket);
                }
            }
        }, 1000);

        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
                e.printStackTrace();
            }
        }
        PredecessorAnswerMessage answerMessage = (PredecessorAnswerMessage) this.answers.get(ticket);
        answerMessage.check();
        return answerMessage.getPredecessor();
    }

    public NodeInfo sendSuccessorRequest(final NodeInfo destination, String node, final NodeInfo sender)throws TimerExpiredException{
        SuccessorRequestMessage successorRequestMessage= new SuccessorRequestMessage(destination, node, sender);
        final int ticket= Router.sendMessage(this.port, successorRequestMessage);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!answers.containsKey(ticket)){
                    SuccessorAnswerMessage successorAnswerMessage = new SuccessorAnswerMessage(sender,null, destination,ticket);
                    successorAnswerMessage.setException(new TimerExpiredException(ticket));
                    answers.put(ticket,successorAnswerMessage);
                    notifyAll();
                }
                else{
                    answers.remove(ticket);
                }
            }
        }, 1000);
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        SuccessorAnswerMessage successorAnswerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        successorAnswerMessage.check();
        return successorAnswerMessage.getSuccessor();
    }

    public void sendPing(final NodeInfo destination, final NodeInfo sender) throws TimerExpiredException{
        PingRequestMessage pingRequestMessage = new PingRequestMessage(destination,sender);
        final int ticket=Router.sendMessage(this.port,pingRequestMessage);
        Timer timer = new Timer(false);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!answers.containsKey(ticket)){
                    PingAnswerMessage successorAnswerMessage = new PingAnswerMessage(sender,destination, ticket);
                    successorAnswerMessage.setException(new TimerExpiredException(ticket));
                    answers.put(ticket,successorAnswerMessage);
                    notifyAll();
                }
                else{
                    answers.remove(ticket);
                }
            }
        }, 1000);
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        PingAnswerMessage pingAnswerMessage = (PingAnswerMessage) answers.get(ticket);
        pingAnswerMessage.check();
    }

    public void addAnswer(int ticket, Message message){
        answers.put(ticket,message);
    }

    public void deleteanswer(int ticket){
        this.answers.remove(ticket);
    }

}

package chord.model;

import chord.Messages.Message;
import chord.Messages.SuccessorRequestMessage;
import chord.network.Router;
import chord.Messages.SuccessorAnswerMessage;
import chord.network.Ticket;

import java.util.*;
import java.lang.*;

public class Node{
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    //attenzione: devo sincerarmi che queste liste mantengano l'ordine di inserimento
    private List<NodeInfo> finger_table;
    private LinkedList<NodeInfo> successor_list;
    private NodeInfo predecessor;
    private boolean initialized;

    //riguardo le answers c'è un discorso da fare che però non mi è ancora del tutto chiaro
    private Hashtable<Integer, Message> answers;



    //this constructor is called when you CREATE and when you JOIN an existent Chord
    public Node(NodeInfo me) {
        this.nodeInfo = me;

        //I need to computer the identifier associated to this node, given the key
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
        this.finger_table = new LinkedList<>();
        this.successor_list = new LinkedList<>();
        this.predecessor = null;
        this.answers = new Hashtable<>();
        this.initialized = false;

    }



    public int getPort(){
        return this.nodeInfo.getPort();
    }

    //not implemented yet[periodic operations to handle changes in the chord]
    public void stabilize(){

        return;
    }
    public void fix_finger(int counter){
        String hashedkey = Utilities.computefinger(this.nodeidentifier, counter);
        NodeInfo nodeInfo = find_successor(hashedkey);
        this.finger_table.set(counter, nodeInfo );
        return;
    }
    public void check_predecessor(){
        //cercherà di pingare il predecessore
        return;
    }

    //method to lookup for a node[ in a recursive manner]
    //ask this node to find the successor of id
    //param = an hashed identifier of the item I want to retrieve
    public NodeInfo find_successor(String hashedkey){

        //first look into the successor list
        for (NodeInfo successor: this.successor_list) {
            String key = successor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
            String nodeidentifier = Utilities.hashfunction(key);
            if (nodeidentifier.compareTo(hashedkey) > 0){
                return successor;
            }
        }

        //else
        //calculating the right finger
        int finger = 1;
        while (Utilities.computefinger(this.nodeidentifier,finger).compareTo(hashedkey)<0){
            finger++;
        }

        //looking into the finger table
        //-2 because the counter starts from 0
        NodeInfo closestSuccessor = this.finger_table.get(finger -2);
        SuccessorRequestMessage successorRequestMessage=new SuccessorRequestMessage(closestSuccessor,hashedkey);
        int ticket;
        ticket=Router.sendMessage(this.getPort(),successorRequestMessage);
        while(!answers.containsKey(ticket)){
            try{
                wait();
            }
            catch(InterruptedException e){
                 e.printStackTrace();
            }
        }
        //qua devo implementare la chiamata ricorsiva
        //e mi serve avere le API del socket layer
        SuccessorAnswerMessage answerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        NodeInfo successor= answerMessage.getSuccessor();
        return successor;

    }

    //when you create a new chord, you have to initialize all the stuff
    public void initialize(){
        for (int i = 0; i<16; i++) {
            finger_table.add(this.nodeInfo);
        }

        for (int i=0; i<4; i++){
            successor_list.add(this.nodeInfo);
        }

        this.initialized = true;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initialize(final NodeInfo myfriend){
        Message message = new SuccessorRequestMessage(myfriend, this.nodeidentifier);
        int ticket;
        ticket = Router.sendMessage(getPort(), message);

        //aspetto finchè non ho ricevuto la risposta
        while (!this.answers.containsKey(ticket)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SuccessorAnswerMessage answerMessage = (SuccessorAnswerMessage) this.answers.get(ticket);
        NodeInfo successor = answerMessage.getSuccessor();
        this.successor_list.add(successor);
        this.finger_table.add(0, successor);

        //now I populate the successor list and the finger table on a separate thread
        new Thread(new Runnable() {
            @Override
            public void run() {

                //first, the successor list
                //QUA CI SONO UN PO' DI CORNER CASES DA GESTIRE
                for (int i = 1; i<4; i++){
                    NodeInfo predecessor = successor_list.get(i-1);
                    String key = predecessor.getIPAddress().concat(Integer.toString(predecessor.getPort()));
                    String hashedkey = Utilities.hashfunction(key);
                    Message message = new SuccessorRequestMessage(successor_list.get(i-1), hashedkey);
                    int ticket;
                    ticket = Router.sendMessage(getPort(), message);

                    //aspetto finchè non ho ricevuto la risposta
                    while (!answers.containsKey(ticket)){
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    SuccessorAnswerMessage answerMessage = (SuccessorAnswerMessage) answers.get(ticket);
                    NodeInfo successor = answerMessage.getSuccessor();

                    //questo è un primo modo di gestire un corner case
                    if (successor.equals(nodeidentifier)){
                        while (i<4){
                            successor_list.addLast(nodeInfo);
                            i++;
                        }
                    } else{
                        successor_list.addLast( successor);
                    }


                }

                //now I populate the finger table







                initialized = true;
            }
        }).start();

    }





}


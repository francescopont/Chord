package chord.model;

import chord.Messages.*;
import chord.network.Router;
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
        NodeInfo predecessor = this.sendPredecessorRequest(this.successor_list.getFirst());

        String key = successor_list.getFirst().getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
        String hashedkey_successor = Utilities.hashfunction(key);

        String potential_key = predecessor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
        String hashedkey_potential_predecessor = Utilities.hashfunction(key);


        if (hashedkey_successor.compareTo(this.nodeidentifier) <0){
            if(hashedkey_potential_predecessor.compareTo(this.nodeidentifier) > 0 || hashedkey_potential_predecessor.compareTo(hashedkey_successor)<0){
                this.successor_list.set(0, predecessor);
            }
        }
        else if (hashedkey_potential_predecessor.compareTo(hashedkey_successor)<0 && hashedkey_potential_predecessor.compareTo(this.nodeidentifier) > 0 ){
            this.successor_list.set(0, predecessor);
        }
        this.sendNotify(this.successor_list.getFirst(),this.nodeInfo);
        //una volta che mi arriva la risposta cosa ci faccio?? niente???

        return;
    }
    public void fix_finger(int counter){
        String hashedkey = Utilities.computefinger(this.nodeidentifier, counter);
        NodeInfo nodeInfo = find_successor(hashedkey);
        this.finger_table.set(counter, nodeInfo );
        return;
    }

    public void check_predecessor(){
        if(predecessor!=null) {
            PingRequestMessage pingRequestMessage = new PingRequestMessage(this.predecessor,this.nodeInfo);
            int ticket;
            ticket=Router.sendMessage(getPort(),pingRequestMessage);
            try{
                wait(1000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
            }
            if(this.answers.containsKey(ticket)){
                this.answers.remove(ticket);
            }
            else{
                this.predecessor=null;
            }

        }
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
        //caso  1: chiave che cerco minore del mio id
        if(hashedkey.compareTo(this.nodeidentifier)<0){
            //vado avanti fino ad arrivare alla teste
            while(Utilities.computefinger(this.nodeidentifier,finger).compareTo(this.nodeidentifier)>0){
                finger++;
            }
            //una volta arrivata alla testa vado avanti finchè non trovo il primo nodo che supera la chiave che sto cercando
            while ((Utilities.computefinger(this.nodeidentifier,finger).compareTo(hashedkey)<0)){
                finger++;
            }

        }
        else{ //chiave che cerco maggiore del mio id
            //vado avanti finchè non trovo il primo nodo che supera la chiave o finchè non finisco l'anello e quindi prendo il nodo più piccolo a cui sono arrivato
            while ((Utilities.computefinger(this.nodeidentifier,finger).compareTo(hashedkey)<0)||(Utilities.computefinger(this.nodeidentifier,finger).compareTo(this.nodeidentifier)>0) ){
                finger++;
        }

        }

        //looking into the finger table
        //-2 because the counter starts from 0
        NodeInfo closestSuccessor = this.finger_table.get(finger -2);
        NodeInfo successor= this.sendSuccessorRequest(closestSuccessor,hashedkey);
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

        NodeInfo successor = this.sendSuccessorRequest(myfriend,this.nodeidentifier);
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
                    NodeInfo successor = sendSuccessorRequest(successor_list.get(i-1),hashedkey);

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

    public void addAnswer(int ticket, Message message){
        answers.put(ticket,message);
    }

    //quando ricevo la notify controllo il mio predecessore e in caso lo aggiorno
    public void notify(NodeInfo potential_predecessor){
        String key = this.predecessor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
        String hashedkey_predecessor = Utilities.hashfunction(key);

        String potential_key = potential_predecessor.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
        String hashedkey_potential_predecessor = Utilities.hashfunction(key);

        if(this.predecessor==null){
            this.predecessor=potential_predecessor;
        }
        else if (hashedkey_predecessor.compareTo(this.nodeidentifier) <0){
            if(hashedkey_potential_predecessor.compareTo(this.nodeidentifier) < 0 && hashedkey_potential_predecessor.compareTo(hashedkey_predecessor)>0){
                this.predecessor=potential_predecessor;
            }
        }
        else if (hashedkey_potential_predecessor.compareTo(hashedkey_predecessor)<0 && hashedkey_potential_predecessor.compareTo(this.nodeidentifier) > 0 ){
            this.predecessor=potential_predecessor;
        }


    }

    public void sendNotify(NodeInfo destination, NodeInfo sender){
        NotifyRequestMessage notifyRequestMessage=new NotifyRequestMessage(destination, sender);
        int ticket= Router.sendMessage(this.getPort(),notifyRequestMessage);
        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
                e.printStackTrace();
            }
        }
    }

    public NodeInfo sendPredecessorRequest(NodeInfo node){
        PredecessorRequestMessage predecessorRequestMessage = new PredecessorRequestMessage(node, this.nodeInfo);
        int ticket = Router.sendMessage(this.getPort(), predecessorRequestMessage);
        while (!this.answers.containsKey(ticket)){
            try{
                wait();
            }catch (InterruptedException  e){
                e.printStackTrace();
            }
        }

        PredecessorAnswerMessage answerMessage = (PredecessorAnswerMessage) this.answers.get(ticket);
        return answerMessage.getPredecessor();

    }

    public NodeInfo sendSuccessorRequest(NodeInfo destination, String node){
        SuccessorRequestMessage successorRequestMessage= new SuccessorRequestMessage(destination, node, this.nodeInfo);
        int ticket= Router.sendMessage(this.getPort(), successorRequestMessage);
        while(!this.answers.containsKey(ticket)){
            try{
                wait();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        SuccessorAnswerMessage successorAnswerMessage= (SuccessorAnswerMessage)this.answers.get(ticket);
        this.answers.remove(ticket);
        return successorAnswerMessage.getSuccessor();
    }



}


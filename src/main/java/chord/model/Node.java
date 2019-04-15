package chord.model;

import chord.Messages.Message;
import chord.network.Router;
import chord.Messages.SuccessorAnswerMessage;

import java.util.*;
import java.lang.*;

public class Node{
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    //attenzione: devo sincerarmi che queste liste mantengano l'ordine di inserimento
    private List<NodeInfo> finger_table;
    private List<NodeInfo> successor_list;
    private NodeInfo predecessor;
    private boolean initialized;

    //riguardo le answers c'è un discorso da fare che però non mi è ancora del tutto chiaro
    private Hashtable<Integer, Message> answers;



    //this constructor is called when you CREATE and when you JOIN a new Chord and when you JOIN an existent Chord
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
        for (NodeInfo nodeInfo: this.successor_list) {
            String key = nodeInfo.getIPAddress().concat(Integer.toString(nodeInfo.getPort()));
            String nodeidentifier = Utilities.hashfunction(key);
            if (nodeidentifier.compareTo(hashedkey) > 0){
                return nodeInfo;
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
        NodeInfo nodeInfo = this.finger_table.get(finger -2);

        //qua devo implementare la chiamata ricorsiva
        //e mi serve avere le API del socket layer
        return nodeInfo;

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

    public void initialize(NodeInfo nodeInfo){
        Message message = new Message(3, true, nodeInfo);
        int ticket;
        ticket = Router.sendMessage(getPort(), message);
        while (!this.answers.containsKey(ticket)){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SuccessorAnswerMessage message1 = (SuccessorAnswerMessage) this.answers.get(ticket);
         NodeInfo successor = message1.getSuccessor();
         this.successor_list.add(successor);

    }





}


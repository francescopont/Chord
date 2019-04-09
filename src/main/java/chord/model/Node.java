package chord.model;

import java.util.*;
import java.lang.*;

public class Node{
    private NodeInfo nodeInfo;
    private String nodeidentifier;
    //attenzione: devo sincerarmi che queste liste mantengano l'ordine di inserimento
    private List<NodeInfo> finger_table;
    private List<NodeInfo> successor_list;
    private NodeInfo predecessor;



    //this constructor is called when you CREATE a new Chord
    public Node(NodeInfo me) {
        this.nodeInfo = me;

        //I need to computer the identifier associated to this node, given the key
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);
        this.finger_table = new LinkedList<>();
        this.successor_list = new LinkedList<>();
        this.predecessor = null;

    }

    //this constructor is called when you JOIN a Chord, given a known node
    public Node(NodeInfo me, NodeInfo myfriend){
        this.nodeInfo = me;

        //I need to computer the identifier associated to this node, given the key
        String key = me.getIPAddress().concat(Integer.toString(me.getPort()));
        this.nodeidentifier = Utilities.hashfunction(key);


        this.finger_table = new LinkedList<NodeInfo>();
        this.successor_list = new LinkedList<NodeInfo>();
        this.predecessor = null;

        //here I must contact myfriend
        //not implemented yet
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
}


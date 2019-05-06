package chord.model;

import java.util.*;

public class FingerTable {
    private List<String> fingers;
    private HashMap<String, NodeInfo> finger_table;
    private String nodeidentifier;

    public FingerTable(String nodeidentifier){
        this.finger_table=new HashMap<>();
        this.fingers = new LinkedList<>();
        this.nodeidentifier = nodeidentifier;
        for (int i=1; i<17; i++){
            String finger = Utilities.computefinger(nodeidentifier,i);
            //the add method appends at the end of the list
            fingers.add(finger);
        }
    }

    public void addEntry(String finger, NodeInfo node){
        finger_table.put(finger,node);
    }

    public NodeInfo closestSuccessor(String key, String node){
        //a set cancels duplicate elements ( which is a valuable behaviour)
        for (
        Set hashes = finger_table.keySet();
        Collections.sort(hashes);

        hashes,new NodeComparator);
        for(int i=0; i<finger_table.size(); i++){
           //trovo il closest
        }
        //da finire
    }*/




}

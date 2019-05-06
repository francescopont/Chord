package chord.model;

import java.util.*;

public class FingerTable {
    private HashMap<String , NodeInfo> finger_table;

    public FingerTable(){
        this.finger_table=new HashMap<>();
    }

    public void addEntry(String finger, NodeInfo node){
        finger_table.put(finger,node);
    }

    /*public NodeInfo closestSuccessor(String key, String node){
        //Collections.sort(finger_table,new NodeComparator);
        for(int i=0; i<finger_table.size(); i++){
           //trovo il closest
        }
        //da finire
    }*/




}

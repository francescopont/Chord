package chord.model;

import java.util.*;

public class FingerTable {

    private List<NodeInfo> finger_table;
    private Comparator comparator;

    public FingerTable(String nodeIdentifier){
        this.finger_table=new LinkedList<>();
        this.comparator= new FingerTableComparator(nodeIdentifier);
    }

    public void addEntry(NodeInfo node){
        finger_table.add(node);
    }


    public NodeInfo closestSuccessor(NodeInfo node){
        Collections.sort(finger_table,comparator);
        NodeInfo predecessor=null;
        for(NodeInfo nodeInfo: finger_table){
           if (comparator.compare(nodeInfo,node)>=0){
               return predecessor;
           }
           predecessor=nodeInfo;
        }
        return predecessor;
        //da finire
    }




}

package chord.model;



import java.util.*;

public class FingerTable{

    private TreeMap<String, NodeInfo> finger_table;
    private Comparator comparator;

    public FingerTable(String nodeIdentifier){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.comparator = comparator;
        this.finger_table=new TreeMap<>(comparator);
    }

    public void addEntry(String key, NodeInfo node){
        if (finger_table.size() < 16){
            finger_table.put(key, node);
        }
    }

    public void modifyEntry(String oldkey, NodeInfo oldnodeInfo,String newkey, NodeInfo newnodeInfo){
        finger_table.remove(oldkey,oldnodeInfo);
        finger_table.put(newkey,newnodeInfo);
    }

    public NodeInfo removeFirst(){
        finger_table.pollFirstEntry();
    }


    public NodeInfo closestSuccessor(String node) {
        synchronized (finger_table){
            NodeInfo closestNode = this.finger_table.floorEntry(node).getValue();
            return closestNode;
        }

        //da finire
    }




}

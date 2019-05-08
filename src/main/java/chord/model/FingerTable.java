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

    //contiamo da 0 a 15
    public void modifyEntry(int position, NodeInfo newnodeInfo){
        Iterator iterator = finger_table.entrySet().iterator();
        int i = position;
        while (iterator.hasNext()){
            i--;
            if (i==0){
                finger_table.remove(iterator.next());
                finger_table.put(newnodeInfo.getHash(),newnodeInfo);
            }
            iterator.next();

        }

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

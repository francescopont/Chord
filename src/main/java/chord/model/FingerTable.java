package chord.model;



import java.util.*;

public class FingerTable{

    private TreeMap<String, NodeInfo> finger_table;
    private Comparator comparator;

    public FingerTable(String nodeIdentifier){
        Comparator comparator = new NodeComparator(nodeIdentifier);
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
        //abbastanza sbatti perchè posso fare replace ma dei valori non dell'intera entry
        //posso fare così perchè tanto poi il comparatore ordinerà lui stesso? e pure la stabilize ?
        //dubito sinceramente
    }

    public void removeFirst(){
        finger_table.pollFirstEntry();
    }


    public NodeInfo closestSuccessor(String node) {
        synchronized (finger_table){
            String predecessor=finger_table.firstKey();
            for(String key: finger_table.keySet()){
                if (comparator.compare(key,node)>=0){
                    return finger_table.get(predecessor);
                }
                predecessor=key;
            }
            return finger_table.get(predecessor);
        }

        //da finire
    }




}

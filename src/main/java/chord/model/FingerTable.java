package chord.model;

import chord.Exceptions.NotInitializedException;

import java.util.*;

public class FingerTable{

    private TreeMap<String, NodeInfo> finger_table;
    private Comparator comparator;
    private boolean initialized;

    public FingerTable(String nodeIdentifier){
        Comparator comparator = new NodeComparator(nodeIdentifier);
        this.comparator = comparator;
        this.finger_table=new TreeMap<>(comparator);
        initialized = false;
    }

    public void addEntry(String key, NodeInfo node){
        if (!initialized){
            finger_table.put(key, node);
            if (finger_table.size() == 16){
                initialized = true;
            }
        }

    }


    public NodeInfo closestSuccessor(String node)throws NotInitializedException {
        if (!initialized){
            throw new NotInitializedException();
        }
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

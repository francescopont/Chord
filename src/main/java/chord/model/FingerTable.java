package chord.model;

import chord.Exceptions.NotInitializedException;

import java.util.*;

public class FingerTable{

    private List<NodeInfo> finger_table;
    private Comparator comparator;
    private boolean initialized;

    public FingerTable(String nodeIdentifier){
        this.finger_table=new LinkedList<>();
        this.comparator= new NodeComparator(nodeIdentifier);
        initialized = false;
    }

    public void addEntry(NodeInfo node){
        if (!initialized){
            finger_table.add(node);
            if (finger_table.size() == 16){
                initialized = true;
            }
        }

    }


    public NodeInfo closestSuccessor(NodeInfo node)throws NotInitializedException {
        if (!initialized){
            throw new NotInitializedException();
        }
        synchronized (finger_table){
            Collections.sort(finger_table,comparator);
            NodeInfo predecessor=null;
            for(NodeInfo nodeInfo: finger_table){
                if (comparator.compare(nodeInfo,node)>=0){
                    return predecessor;
                }
                predecessor=nodeInfo;
            }
            return predecessor;
        }

        //da finire
    }




}

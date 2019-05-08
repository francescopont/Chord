package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.*;

public class SuccessorList {

    private TreeMap<String, NodeInfo> successor_list;
    private Comparator comparator;

    public SuccessorList(String nodeIdentifier){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.comparator = comparator;
        this.successor_list = new TreeMap<String, NodeInfo>(comparator);
    }

    public void addEntry(String key, NodeInfo node){
        if (successor_list.size() < 4){
            successor_list.put(key, node);
        }
    }

    public NodeInfo getFirst(){
        String key=successor_list.firstKey();
        return successor_list.get(key);
    }

    public void setFirst(String key, NodeInfo nodeInfo){
        this.successor_list.pollFirstEntry();
        this.successor_list.put(key,nodeInfo);
    }

    public NodeInfo removeFirst(){
        return successor_list.pollFirstEntry().getValue();
    }

    public void modifyEntry(String oldkey, NodeInfo oldnodeInfo,String newkey, NodeInfo newnodeInfo){
        successor_list.remove(oldkey,oldnodeInfo);
        successor_list.put(newkey,newnodeInfo);
    }


    public NodeInfo getSuccessor(String node) throws SuccessorListException{
        synchronized (successor_list){
            NodeInfo successor = this.successor_list.ceilingEntry(node).getValue();
            if(successor == null){
                throw new SuccessorListException();
            }
            return successor;

        }

    }
}

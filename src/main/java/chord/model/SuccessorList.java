package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.*;

public class SuccessorList {

    private TreeMap<String, NodeInfo> successor_list;
    private Comparator comparator;

    public SuccessorList(String nodeIdentifier){
        Comparator comparator = new NodeComparator(nodeIdentifier);
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

    public void setFirt(String key, NodeInfo nodeInfo){
        //come faccio??
    }

    public NodeInfo removeFirst(){
        return successor_list.pollFirstEntry().getValue();
    }

    public void modifyEntry(String oldkey, NodeInfo oldnodeInfo,String newkey, NodeInfo newnodeInfo){
        successor_list.remove(oldkey,oldnodeInfo);
        successor_list.put(newkey,newnodeInfo);
        //abbastanza sbatti perchè posso fare replace ma dei valori non dell'intera entry
        //posso fare così perchè tanto poi il comparatore ordinerà lui stesso? e pure la stabilize ?
        //dubito sinceramente
    }


    public NodeInfo getSuccessor(String node) throws SuccessorListException{
        synchronized (successor_list){
            for(String key: successor_list.keySet()){
                if (comparator.compare(key,node)>=0){
                    return successor_list.get(key);
                }
            }
            throw new SuccessorListException();
        }

    }
}

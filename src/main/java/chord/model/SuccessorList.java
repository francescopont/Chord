package chord.model;

import chord.Exceptions.NotInitializedException;
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

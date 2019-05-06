package chord.model;

import chord.Exceptions.NotInitializedException;
import chord.Exceptions.SuccessorListException;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class SuccessorList {

    private List<NodeInfo> successor_list;
    private Comparator comparator;
    private boolean initialized;

    public SuccessorList(String nodeIdentifier){
        this.successor_list =new LinkedList<>();
        this.comparator= new NodeComparator(nodeIdentifier);
        initialized = false;
    }

    public void addEntry(NodeInfo node){
        if (!initialized){
            successor_list.add(node);
            if (successor_list.size() == 4){
                initialized = true;
            }
        }
    }


    public NodeInfo successor(NodeInfo node) throws SuccessorListException,NotInitializedException{
        if(!initialized){
            throw new NotInitializedException();
        }
        synchronized (successor_list){
            Collections.sort(successor_list,comparator);
            for(NodeInfo nodeInfo: successor_list){
                if (comparator.compare(nodeInfo,node)>=0){
                    return nodeInfo;
                }
            }
            throw new SuccessorListException();
        }

    }
}

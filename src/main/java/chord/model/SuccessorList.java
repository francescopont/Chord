package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.*;

public class SuccessorList {

    private final TreeMap<String, NodeInfo> successor_list;

    public SuccessorList(String nodeIdentifier){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.successor_list = new TreeMap<String, NodeInfo>(comparator);
    }

    public synchronized void addEntry(String key, NodeInfo node){
        if (successor_list.size() < 4){
            successor_list.put(key, node);
        }
    }

    //contiamo da 0 a 15
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        Iterator<String> iterator = successor_list.keySet().iterator();
        int i = position;
        boolean found = false;
        while (iterator.hasNext() && !found){
            if (i==0){
                found = true;
            }
            if(!found){
                i--;
                iterator.next();
            }
        }
        successor_list.remove(iterator.next());
        successor_list.put(newnodeInfo.getHash(),newnodeInfo);
    }

    public synchronized NodeInfo closestSuccessor(String node) throws SuccessorListException{
        NodeInfo successor = this.successor_list.ceilingEntry(node).getValue();
        if(successor == null){
            throw new SuccessorListException();
        }
        return successor;
    }

    //to get a specific nodeinfo (indexes go from 0 to 4)
    public NodeInfo getElement(int position){
        Iterator iterator = successor_list.keySet().iterator();
        int i = position;
        boolean found = false;
        while (iterator.hasNext() && !found){
            if (i==0){
                found = true;
            }
            if(!found){
                i--;
                iterator.next();
            }
        }
        return successor_list.get(iterator.next());
    }


}


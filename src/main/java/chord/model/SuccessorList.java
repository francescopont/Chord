package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.*;

public class SuccessorList {
    private final TreeMap<String, NodeInfo> successorList;

    public SuccessorList(String nodeIdentifier){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.successorList = new TreeMap<String, NodeInfo>(comparator);
    }

    public synchronized void addEntry(String key, NodeInfo node){
        if (successorList.size() < 4){
            successorList.put(key, node);
        }
    }

    //contiamo da 0 a 15
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        Iterator<String> iterator = successorList.keySet().iterator();
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
        successorList.remove(iterator.next());
        successorList.put(newnodeInfo.getHash(),newnodeInfo);
    }

    public synchronized NodeInfo closestSuccessor(String node) throws SuccessorListException{
        NodeInfo successor = this.successorList.ceilingEntry(node).getValue();
        if(successor == null){
            throw new SuccessorListException();
        }
        return successor;
    }

    //to get a specific nodeinfo (indexes go from 0 to 4)
    public NodeInfo getElement(int position){
        Iterator iterator = successorList.keySet().iterator();
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
        return successorList.get(iterator.next());
    }

    //useful for testing
    //to print the state of the successorList
    public void printTable(){
        int i=1;
        System.out.println("SUCCESSOR LIST");
        for (String finger: this.successorList.keySet()){
            System.out.println("finger " + i + ": " + finger);
            i++;
        }
    }


}


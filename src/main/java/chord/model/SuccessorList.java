package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class SuccessorList {
    private final TreeMap<Finger, NodeInfo> successorList;

    public SuccessorList(String nodeIdentifier){
        this.successorList = new TreeMap<Finger, NodeInfo>(new FingerTableComparator(nodeIdentifier));
    }

    public synchronized void addEntry( NodeInfo node){
        if (successorList.size() < 4){
            if(successorList.size()==0){
                Finger finger=new Finger(node.getHash(), 0);
                finger.setInitializing(true);
                successorList.put(finger,node);
                finger.setInitializing(false);
            }
            int position=this.successorList.lastKey().getPosition()+1;
            Finger finger = new Finger(node.getHash(), position);
            finger.setInitializing(true);
            successorList.put(finger, node);
            finger.setInitializing(false);
        }
    }

    //contiamo da 0 a 15
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        Iterator<Finger> iterator = successorList.keySet().iterator();
        while (iterator.hasNext()){
            Finger finger = iterator.next();
            if (finger.getPosition() == position){
                finger.setHash(newnodeInfo.getHash());
                successorList.put(finger,newnodeInfo);
            }
        }

    }

    public synchronized NodeInfo closestSuccessor(String node) throws SuccessorListException{
        Finger finger = new Finger(node);
        NodeInfo successor = this.successorList.ceilingEntry(finger).getValue();
        if(successor == null){
            throw new SuccessorListException();
        }
        return successor;
    }

    //to get a specific nodeinfo (indexes go from 0 to 4)
    public NodeInfo getElement(int position){
        for (Map.Entry<Finger, NodeInfo> entry : successorList.entrySet()) {
            if (entry.getKey().getPosition() == position) {
                return entry.getValue();
            }
        }

        //if the method is called properly, this instruction is never reached
        return successorList.lastEntry().getValue();
    }

    public NodeInfo getLastElement(){
        return this.successorList.lastEntry().getValue();
    }

    public NodeInfo getFirstElement(){
        return this.successorList.firstEntry().getValue();
    }

    //useful for testing
    //to print the state of the successorList
    public void printTable(){
        int i=0;
        System.out.println("SUCCESSOR LIST");
        for (Finger finger: this.successorList.keySet()){
            System.out.println("finger " + finger.getPosition() + ": " + finger.getHash());
            i++;
        }
    }


}


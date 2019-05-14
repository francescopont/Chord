package chord.model;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class FingerTable{
    private final TreeMap<Finger, NodeInfo> fingerTable;

    //constructor
    public FingerTable(String nodeIdentifier){
        this.fingerTable =new TreeMap<Finger, NodeInfo>(new FingerTableComparator(nodeIdentifier));
    }

    //to add an entry when the finger table is not full
    //this
    public synchronized void addFinger(NodeInfo node){
        if (fingerTable.size() < Utilities.numberOfBit()){
            if(fingerTable.size()==0){
                Finger finger= new Finger(node.getHash(),0);
                finger.setInitializing(true);
                fingerTable.put(finger,node);
                finger.setInitializing(false);
            }
            int position= this.fingerTable.lastKey().getPosition()+1;
            Finger finger = new Finger(node.getHash(),position);
            finger.setInitializing(true);
            fingerTable.put(finger, node);
            finger.setInitializing(false);
        }
    }

    //contiamo da 0 A 15
    public synchronized void modifyFinger(int position,  NodeInfo newnodeInfo){
        Iterator<Finger> iterator = fingerTable.keySet().iterator();
        boolean end = false;
        while (!end && iterator.hasNext()){
            Finger finger = iterator.next();
            if (finger.getPosition() == position){
                finger.setHash(newnodeInfo.getHash());
                fingerTable.put(finger,newnodeInfo);
                end = true;
            }
        }

    }

    //to get the closest precedessor of a given nodeidentifier
    public synchronized NodeInfo closestPredecessor(String node) {
        Finger finger = new Finger(node);
        return this.fingerTable.floorEntry(finger).getValue();
    }


    //useful for testing
    public FingerTable(String nodeIdentifier, Map<Finger, NodeInfo> fingers){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.fingerTable =new TreeMap<>(comparator);
        this.fingerTable.putAll(fingers);
    }

    //to check if every key maps to the right nodeinfo ( a bit trivial)
    public boolean checkMapping(){
        for (Map.Entry<Finger,NodeInfo> entry : this.fingerTable.entrySet()){
            if (!(entry.getKey().getHash().equals(entry.getValue().getHash()))){
                return false;
            }
        }
        return true;
    }

    //to get a specific nodeinfo
    //positions go from 0 to 15
    public NodeInfo getFinger(int position){
        for (Map.Entry<Finger, NodeInfo> entry : fingerTable.entrySet()) {
            if (entry.getKey().getPosition() == position) {
                return entry.getValue();
            }
        }

        //if the method is called properly, this instruction is never reached
        return fingerTable.lastEntry().getValue();
    }



    //to print the state of the fingertable
    public void printTable(){
        System.out.println("FINGER TABLE");
        for (Finger finger: this.fingerTable.keySet()){
            System.out.println("finger " + finger.getPosition() + ": " + finger.getHash());

        }
    }

    // to remove a finger ( it should never be used without calling addfinger immediately after
    public synchronized NodeInfo removeFinger(int position){
        Iterator<Finger> iterator = fingerTable.keySet().iterator();
        NodeInfo nodeInfo = null;
        while (iterator.hasNext()){
            Finger finger = iterator.next();
            if (finger.getPosition() == position){
                 nodeInfo = fingerTable.remove(finger);

                //check the correctness of the other fingers
                Iterator<Finger> tailIterator = fingerTable.tailMap(finger,false).keySet().iterator();
                while (tailIterator.hasNext()){
                    iterator.next().setPosition(position);
                    position++;

                }
            }
        }
        return nodeInfo;
    }

    public boolean containsFinger(String key){
        for (Finger finger : fingerTable.keySet()) {
            if (finger.getHash() == key) {
                return true;
            }
        }
        return false;
    }

}

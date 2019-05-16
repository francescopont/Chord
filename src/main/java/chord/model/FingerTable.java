package chord.model;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class FingerTable{
    private final TreeMap<String, NodeInfo> map;
    private LinkedList<NodeInfo> successors;
    private String node;

    //constructor
    public FingerTable(String nodeIdentifier){
        this.map = new TreeMap <>(new FingerTableComparator(nodeIdentifier));
        this.successors = new LinkedList<>();
        this.node =nodeIdentifier;
    }

    //to add an entry when the finger table is not full

    public synchronized void addFinger(NodeInfo node){
        if (successors.size() < Utilities.numberOfBit()) {
            successors.addLast(node);
        }
        /*if (fingerTable.size() < Utilities.numberOfBit()){
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
        */
    }

    //contiamo da 0 A 15
    public synchronized void modifyFinger(int position,  NodeInfo newnodeInfo){
        this.successors.set(position, newnodeInfo);
        /*Iterator<Finger> iterator = fingerTable.keySet().iterator();
        boolean end = false;
        while (!end && iterator.hasNext()){
            Finger finger = iterator.next();
            if (finger.getPosition() == position){
                finger.setHash(newnodeInfo.getHash());
                fingerTable.put(finger,newnodeInfo);
                end = true;
            }
        }
        */

    }

    //to get the closest precedessor of a given nodeidentifier
    public synchronized NodeInfo closestPredecessor(String node) {
        this.map.clear();
        for (NodeInfo nodeInfo : this.successors){
            this.map.put(nodeInfo.getHash(), nodeInfo);
        }

        Map.Entry<String, NodeInfo> successor = this.map.floorEntry(node);
        return successor.getValue();

        //Finger finger = new Finger(node);
        //return this.fingerTable.floorEntry(finger).getValue();
    }

    //to print the state of the fingertable
    public void printTable() {
        int i = 0;
        System.out.println("FINGER TABLE");
        for (NodeInfo nodeInfo : this.successors) {
            System.out.println("finger " + i + ": " + nodeInfo.getHash());
            i++;
        }
    }

    public FingerTable(String nodeIdentifier, Map<Finger, NodeInfo> fingers){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.successors =new LinkedList<>();
        this.map = new TreeMap<>(comparator);
        this.node = nodeIdentifier;
    }


    //useful for testing


    //to check if every key maps to the right nodeinfo ( a bit trivial)
    public boolean checkMapping(){
        return true;
    }

    //to get a specific nodeinfo
    //positions go from 0 to 15
    public NodeInfo getFinger(int position){
        return this.successors.get(position);
    }





    // to remove a finger ( it should never be used without calling addfinger immediately after
    public synchronized NodeInfo removeFinger(int position){
        return this.successors.remove(position);
    }

    public boolean containsFinger(String key){
        for (NodeInfo nodeInfo: this.successors) {
            if (nodeInfo.getHash() == key) {
                return true;
            }
        }
        return false;
    }


}

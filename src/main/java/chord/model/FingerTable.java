package chord.model;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class FingerTable{
    private final TreeMap<String, NodeInfo> map;
    private LinkedList<NodeInfo> fingers;
    private String node;

    //constructor
    public FingerTable(String nodeIdentifier){
        this.map = new TreeMap <>(new FingerTableComparator(nodeIdentifier));
        this.fingers = new LinkedList<>();
        this.node =nodeIdentifier;
    }

    //to add an entry when the finger table is not full
    public synchronized void addFinger(NodeInfo node){
        if (fingers.size() < Utilities.numberOfBit()) {
            fingers.addLast(node);
        }
    }

    //contiamo da 0 A 15
    public synchronized void modifyFinger(int position,  NodeInfo newnodeInfo){
        this.fingers.set(position, newnodeInfo);
    }

    //positions go from 0 to 15
    public NodeInfo getFinger(int position){
        return this.fingers.get(position);
    }

    //to get the closest precedessor of a given nodeidentifier
    public synchronized NodeInfo closestPredecessor(String node) {
        this.map.clear();
        for (NodeInfo nodeInfo : this.fingers){
            this.map.put(nodeInfo.getHash(), nodeInfo);
        }
        Map.Entry<String, NodeInfo> successor = this.map.floorEntry(node);
        return successor.getValue();
    }

    //to print the state of the fingertable
    public void printTable() {
        int i = 0;
        System.out.println("FINGER TABLE");
        for (NodeInfo nodeInfo : this.fingers) {
            System.out.println("finger " + i + ": " + nodeInfo.getHash());
            i++;
        }
    }


    //useful for testing

    //to get a specific nodeinfo

    // to remove a finger ( it should never be used without calling addfinger immediately after
    public synchronized NodeInfo removeFinger(int position){
        return this.fingers.remove(position);
    }

    public boolean containsFinger(String key){
        for (NodeInfo nodeInfo: this.fingers) {
            if (nodeInfo.getHash().equals(key)) {
                return true;
            }
        }
        return false;
    }


}

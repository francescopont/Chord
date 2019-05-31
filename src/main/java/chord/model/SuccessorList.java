package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class SuccessorList {
    private final TreeMap<String, NodeInfo> map;
    private LinkedList<NodeInfo> successors;
    private String node;
    // this boolean indicates if I'm alone on the chord
    private boolean alone;

    //constructor
    public SuccessorList(String nodeIdentifier){
        this.map = new TreeMap<>(new FingerTableComparator(nodeIdentifier));
        this.successors = new LinkedList<>();
        this.node =nodeIdentifier;
        this.alone = true;
    }

    //to add an entry in the last position
    public void addEntry( NodeInfo node){
        if (successors.size() < 4){
            successors.addLast(node);
        }
    }

    //we count from 0 to 3
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        if (position == 0 && newnodeInfo.getHash().equals(this.node)){
            System.out.println("stai creando un bug");
            setAlone(true);
        }
        if (position == 0 && !(newnodeInfo.getHash().equals(this.node))){
            setAlone(false);
        }
        this.successors.set(position, newnodeInfo);

    }

    public synchronized NodeInfo closestSuccessor(String node) throws SuccessorListException{
        this.map.clear();
        for (NodeInfo nodeInfo : this.successors){
            this.map.put(nodeInfo.getHash(), nodeInfo);
        }
        Map.Entry<String, NodeInfo> successor = this.map.ceilingEntry(node);
        if(successor == null){
            throw new SuccessorListException();
        }
        return successor.getValue();
    }

    //to get a specific nodeinfo (indexes go from 0 to 3)
    public NodeInfo getElement(int position){
        return this.successors.get(position);
    }

    public NodeInfo getLastElement(){
        return this.successors.getLast();
    }

    public NodeInfo getFirstElement(){
        return successors.getFirst();
    }

    public void removeLast(){
        this.successors.removeLast();
    }

    public boolean isAlone() { return alone; }

    public void setAlone(boolean alone) {
        this.alone = alone;
    }


    //useful for testing
    //to print the state of the map
    public void printTable(){
        int i=0;
        System.out.println("SUCCESSOR LIST ");
        System.out.println("size: " + this.successors.size());
        for (NodeInfo nodeInfo: this.successors){
            System.out.println("finger " + i + " : " + nodeInfo.getHash() );
            i++;
        }
    }


    //useful for testing
    //getters
    public TreeMap<String, NodeInfo> getMap() {
        return map;
    }

    public LinkedList<NodeInfo> getSuccessors() {
        return successors;
    }

    public String getNode() {
        return node;
    }

    public boolean containsSuccessor(String key){
        for (NodeInfo nodeInfo: this.successors) {
            if (nodeInfo.getHash().equals(key)) {
                return true;
            }
        }
        return false;
    }



}


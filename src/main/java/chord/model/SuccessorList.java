package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class SuccessorList {
    private final TreeMap<String, NodeInfo> map;
    private LinkedList<NodeInfo> successors;
    private String node;

    public SuccessorList(String nodeIdentifier){
        this.map = new TreeMap<>(new FingerTableComparator(nodeIdentifier));
        this.successors = new LinkedList<>();
        this.node =nodeIdentifier;
    }

    //to add an entry in the last position
    public synchronized void addEntry( NodeInfo node){
        if (successors.size() < 4){
            successors.addLast(node);
        }
    }

    //contiamo da 0 a 3
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        System.out.println("sto cambiando tramite modify la posizione "+ position);
        System.out.println("voglio aggiungere l'hash "+ newnodeInfo.getHash());
        printTable();
        this.successors.set(position, newnodeInfo);
        System.out.println("ho cambiato la posizione");
        printTable();
    }

    public synchronized NodeInfo closestSuccessor(String node) throws SuccessorListException{
        System.out.println("ho chiamato la closest successor rispetto alla chiave " + node);
        this.map.clear();
        for (NodeInfo nodeInfo : this.successors){
            this.map.put(nodeInfo.getHash(), nodeInfo);
        }

        Map.Entry<String, NodeInfo> successor = this.map.ceilingEntry(node);
        if(successor == null){
            throw new SuccessorListException();
        }
        System.out.println("ho trovato come closest successor " + successor.getValue().getHash());
        return successor.getValue();
    }

    //to get a specific nodeinfo (indexes go from 0 to 4)
    public NodeInfo getElement(int position){
        return this.successors.get(position);
    }

    public NodeInfo getLastElement(){
        return this.successors.getLast();
    }

    public NodeInfo getFirstElement(){
        return this.successors.getFirst();
    }

    //useful for testing
    //to print the state of the map
    public void printTable(){
        int i=0;
        System.out.println("SUCCESSOR LIST OF "+ node);
        System.out.println("size: " + this.successors.size());
        for (NodeInfo nodeInfo: this.successors){
            System.out.println("finger " + i + " : " + nodeInfo.getHash() );
            i++;
        }
    }


}


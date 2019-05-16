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
            /*if(map.size()==0){
                Finger finger=new Finger(node.getHash(), 0);
                finger.setInitializing(true);
                map.put(finger,node);
                finger.setInitializing(false);
            }
            int position=this.map.lastKey().getPosition()+1;
            Finger finger = new Finger(node.getHash(), position);
            finger.setInitializing(true);
            map.put(finger, node);
            finger.setInitializing(false);
            */
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


        /*
        Iterator<Finger> iterator = map.keySet().iterator();

        boolean end = false;
        Finger finger = null;
        while (!end && iterator.hasNext()){
            finger = iterator.next();
            if (finger.getPosition() == position){
                System.out.println("sto cambiando tramite modify la posizione "+ position);
                System.out.println("voglio aggiungere l'hash "+ newnodeInfo.getHash());
                end = true;

            }
        }

        finger.setHash(newnodeInfo.getHash());
        printTable();
        NodeInfo nodeInfo = map.put(finger, newnodeInfo);
        if (nodeInfo == null){
            System.out.println("hai un problema");
        }
        printTable();

        finger.setInitializing(true);
        map.remove(finger);
        printTable();
        Finger finger1 = new Finger(newnodeInfo.getHash(), finger.getPosition());
        finger1.setInitializing(true);
        map.put(finger1,newnodeInfo);
        printTable();
        finger1.setInitializing(false);

        printTable();
        */
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


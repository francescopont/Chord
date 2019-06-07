package chord.model;

import chord.Exceptions.SuccessorListException;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class which represents the successor list of an associated node
 */
public class SuccessorList {
    /**
     * Support data structure to sort the fingers
     */
    private final TreeMap<String, NodeInfo> map;
    /**
     * List of successors of the node
     */
    private LinkedList<NodeInfo> successors;
    /**
     * The identifier of the node
     */
    private String node;
    /**
     * Boolean which indicates if the node is the only in the Chord or not
     */
    private boolean alone;


    public SuccessorList(String nodeIdentifier){
        this.map = new TreeMap<>(new FingerTableComparator(nodeIdentifier));
        this.successors = new LinkedList<>();
        this.node =nodeIdentifier;
        this.alone = true;
    }

    /**
     * Add a new node in the last position
     * @param node information of the node to insert
     */
    public void addEntry( NodeInfo node) {
        if (successors.size() < 4) {
            successors.addLast(node);
        }

    }

    /**
     * Modify the successor list by inserting the new node in the indicated position
     * Position from 0 to 3
     * @param position of the node to modify
     * @param newnodeInfo information of the new node
     */
    public synchronized void modifyEntry(int position, NodeInfo newnodeInfo){
        if (position == 0 && newnodeInfo.getHash().equals(this.node)){
            setAlone(true);
        }
        if (position == 0 && !(newnodeInfo.getHash().equals(this.node))){
            setAlone(false);
        }
        this.successors.set(position, newnodeInfo);
    }

    /**
     * Get the smallest node, if it exists, among the ones bigger than the given node
     * @param node whose successor the caller is looking for
     * @return the smallest node among the ones bigger than the given node
     * @throws SuccessorListException Exception thrown if the successor list does not contain such node
     */
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

    /**
     * Return an element in a specific position
     * @param position from 0 to 3
     * @return element in that position
     */
    public NodeInfo getElement(int position){
        return this.successors.get(position);
    }

    /**
     * Get the last element of the successor list
     * @return the last element
     */
    public NodeInfo getLastElement(){
        return this.successors.getLast();
    }

    /**
     * Get the first element of the successor list
     * @return the first element
     */
    public NodeInfo getFirstElement(){
        return successors.getFirst();
    }

    public boolean isAlone() { return alone; }

    public void setAlone(boolean alone) {
        this.alone = alone;
    }

    /**
     * Print the status of the successor list
     */
    public void printTable(){
        int i=0;
        System.out.println("SUCCESSOR LIST ");
        System.out.println("size: " + this.successors.size());
        for (NodeInfo nodeInfo: this.successors){
            System.out.println("finger " + i + " : " + nodeInfo.getHash() );
            i++;
        }
    }

    /**
     *Methods useful for testing
     */

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


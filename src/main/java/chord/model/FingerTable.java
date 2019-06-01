package chord.model;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class represents the finger table of an associated node
 */
public class FingerTable{
    /**
     * Support data structure to sort the fingers
     */
    private final TreeMap<String, NodeInfo> map;
    /**
     * List of information of fingers
     */
    private LinkedList<NodeInfo> fingers;
    /**
     * The identifier of the node
     */
    private String node;

    public FingerTable(String nodeIdentifier){
        this.map = new TreeMap <>(new FingerTableComparator(nodeIdentifier));
        this.fingers = new LinkedList<>();
        this.node =nodeIdentifier;
    }

    /**
     * Add a node information when initializing the finger table
     * @param node Node information of the node to add (Ip address and port)
     */
    public void addFinger(NodeInfo node){
        if (fingers.size() < Utilities.numberOfBit()) {
            fingers.addLast(node);
        }
    }

    /**
     * Modify the node information associated to a finger
     * @param position of the finger to modify (from 0 to 15)
     * @param newnodeInfo new information to insert
     */
    public synchronized void modifyFinger(int position,  NodeInfo newnodeInfo){
        this.fingers.set(position, newnodeInfo);
    }

    /**
     * Get the information associated with a specific finger
     * @param position of the finger (from 0 to 15)
     * @return the node information : IP address and port
     */
    public NodeInfo getFinger(int position){
        return this.fingers.get(position);
    }

    /**
     * Get the biggest node among the ones smaller than the given node
     * @param node whose predecessor the caller is looking for
     * @return the biggest node among the ones smaller than the given node
     */
    public synchronized NodeInfo closestPredecessor(String node) {
        this.map.clear();
        for (NodeInfo nodeInfo : this.fingers){
            this.map.put(nodeInfo.getHash(), nodeInfo);
        }
        Map.Entry<String, NodeInfo> predecessor = this.map.lowerEntry(node);
        return predecessor.getValue();
    }

    /**
     * Print the finger table
     */
    public void printTable() {
        int i = 0;
        System.out.println("FINGER TABLE");
        for (NodeInfo nodeInfo : this.fingers) {
            System.out.println("finger " + i + ": " + nodeInfo.getHash());
            i++;
        }
    }


    /**
     *Methods used only in testing
     */

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

    public LinkedList<NodeInfo> getFingers() {
        return fingers;
    }

    public String getNode() {
        return node;
    }


}

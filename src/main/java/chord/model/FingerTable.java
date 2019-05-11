package chord.model;
import java.util.*;

public class FingerTable{
    private final TreeMap<String, NodeInfo> finger_table;

    //constructor
    public FingerTable(String nodeIdentifier){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.finger_table=new TreeMap<String, NodeInfo>(comparator);
    }

    //to add an entry when the finger table is not full
    public synchronized void addFinger(String key, NodeInfo node){
        if (finger_table.size() < Utilities.numberOfBit()){
            finger_table.put(key, node);
        }
    }

    //contiamo da 0 A 15
    public synchronized void modifyFinger(int position, NodeInfo newnodeInfo){
        Iterator<String> iterator = finger_table.keySet().iterator();
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
        finger_table.remove(iterator.next());
        finger_table.put(newnodeInfo.getHash(), newnodeInfo);
    }

    //to get the closest precedessor of a given nodeidentifier
    public synchronized NodeInfo closestPredecessor(String node) {
        return this.finger_table.floorEntry(node).getValue();
    }


    //useful for testing
    public FingerTable(String nodeIdentifier, Map<String, NodeInfo> fingers){
        Comparator comparator = new FingerTableComparator(nodeIdentifier);
        this.finger_table=new TreeMap<>(comparator);
        this.finger_table.putAll(fingers);
    }

    //to check if every key maps to the right nodeinfo ( a bit trivial)
    public boolean checkMapping(){
        for (Map.Entry<String,NodeInfo> entry : this.finger_table.entrySet()){
            if (!(entry.getKey().equals(entry.getValue().getHash()))){
                return false;
            }
        }
        return true;
    }

    //to get a specific nodeinfo
    public NodeInfo getFinger(int position){
        Iterator iterator = finger_table.keySet().iterator();
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
        return finger_table.get(iterator.next());
    }

    //to print the state of the fingertable
    public void printTable(){
        int i=1;
        System.out.println("FINGER TABLE");
        for (String finger: this.finger_table.keySet()){
            System.out.println("finger " + i + ": " + finger);
            i++;
        }
    }

    // to remove a finger ( it should never be used without calling addfinger immediately after
    public synchronized NodeInfo removeFinger(int position){
        Iterator<String> iterator = finger_table.keySet().iterator();
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
        return finger_table.remove(iterator.next());
    }

    public boolean containsFinger(String key){
        return this.finger_table.containsKey(key);
    }

}

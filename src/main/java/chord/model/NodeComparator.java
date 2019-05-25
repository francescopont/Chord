package chord.model;

import java.util.Comparator;

public class NodeComparator implements Comparator<String> {

    //the hash which we consider as the "0" on the ring
    private String nodeidentifier;

    //constructor
    public NodeComparator(String nodeidentifier) {
        this.nodeidentifier = nodeidentifier;
    }



    // this comparator set the nodeidentifier as the smallest value on the ring
    @Override
    public int compare(String hash1, String hash2){
        //case1: both values are greater than the nodeidentifier
        if (hash1.compareTo(nodeidentifier)>=0 && hash2.compareTo(nodeidentifier) >=0){
            //we compare them as usual
            return hash1.compareTo(hash2);
        }
        //case 2: the first value is smaller than nodeidentifier, the second is greater
        if (hash1.compareTo(nodeidentifier)<0 && hash2.compareTo(nodeidentifier) >=0){
            //the first one must be returned
            return 1;
        }
        //case 3: the first value is greater than nodeidentifier, the second is smaller
        if (hash1.compareTo(nodeidentifier) >=0 && hash2.compareTo(nodeidentifier) <0){
            //the second one must be returned
            return -1;
        }
        //case 4: both values are smaller than the nodeidentifier
        if (hash1.compareTo(nodeidentifier)<0 && hash2.compareTo(nodeidentifier) <0){
            //we compare them as usual
            return hash1.compareTo(hash2);
        }
        //this instruction is never reached
        return 0;
    }
}

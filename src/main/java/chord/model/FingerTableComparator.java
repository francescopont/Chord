package chord.model;

import java.util.Comparator;
import java.util.Map;

public class FingerTableComparator implements Comparator<String> {
    //the hash where we start from the ring
    private String nodeidentifier;

    public FingerTableComparator(String nodeidentifier) {
        this.nodeidentifier = nodeidentifier;
    }

    @Override
    public int compare(String hash1, String hash2){
        //case1: both values are

        if (hash1.compareTo(nodeidentifier)>0 && hash2.compareTo(nodeidentifier) > 0)
    }
}

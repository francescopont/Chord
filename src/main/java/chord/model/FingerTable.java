package chord.model;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class FingerTable {
    private LinkedHashMap<String , NodeInfo> finger_table;

    public FingerTable(){
        this.finger_table=new LinkedHashMap<>();
    }

    public void addEntry(String finger, NodeInfo node){
        finger_table.put(finger,node);
    }



}

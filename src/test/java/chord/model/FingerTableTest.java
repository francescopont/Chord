package chord.model;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class FingerTableTest {
    FingerTable fingerTable;
    FingerTable fingerTable2;


    @Before
    public void setUp() throws Exception {
        Map<String, NodeInfo> nodeInfoMap = new HashMap<>();
        String hash = "0000";
        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger(hash,i));
            nodeInfoMap.put(nodeInfo.getHash(),nodeInfo);
        }

        //tested class
        // we use the fake constructor
        fingerTable = new FingerTable("0000", nodeInfoMap );
        fingerTable.printTable();

        //fingertable for addFinger
        Map<String, NodeInfo> nodeInfoMap2 = new HashMap<>();
        String hash2 = "0000";
        for (int i = 0; i< 15; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger(hash,i));
            nodeInfoMap2.put(nodeInfo.getHash(),nodeInfo);
        }

        //tested class
        // we use the fake constructor
        fingerTable2 = new FingerTable("0000", nodeInfoMap2 );
        fingerTable2.printTable();
    }



    @Test
    public void addEntryTest(){
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable2.addFinger(nodeInfo.getHash(),nodeInfo);

        //asserts
        assert(fingerTable2.checkMapping());
        assert (fingerTable2.containsFinger(nodeInfo.getHash()));
        fingerTable2.printTable();
    }

    @Test
    public void closestPredecessorTest(){
        NodeInfo nodeInfo = new NodeInfo("0004");
        NodeInfo response = fingerTable.closestPredecessor(nodeInfo.getHash());
        assert (response.equals(nodeInfo));
        fingerTable.printTable();
    }

    @Test
    public void modifyEntryTest(){
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable.modifyFinger(0,nodeInfo);

        //asserts
        assert(fingerTable.checkMapping());
        assert (fingerTable.containsFinger(nodeInfo.getHash()));
        assert (!(fingerTable.containsFinger(Utilities.computefinger("0000",0))));
        fingerTable.printTable();
    }

}

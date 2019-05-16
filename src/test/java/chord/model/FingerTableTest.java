package chord.model;

import org.junit.Before;
import org.junit.Test;

public class FingerTableTest {
    FingerTable fingerTable;

    @Before
    public void setUp() throws Exception {
        String hash = "0000";
        fingerTable = new FingerTable(hash);

        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger(hash,i));
            fingerTable.addFinger(nodeInfo);
        }

        fingerTable.printTable();
    }



    @Test
    public void addFingerTest(){
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable.addFinger(nodeInfo);

        //asserts
        assert (!fingerTable.containsFinger(nodeInfo.getHash()));
    }

    @Test
    public void closestPredecessorTest(){

        NodeInfo nodeInfo = new NodeInfo("5000");
        NodeInfo nodeInfo1= new NodeInfo("7000");
        fingerTable.modifyFinger(2, nodeInfo);
        NodeInfo response = fingerTable.closestPredecessor(nodeInfo1.getHash());
        //assert (response.equals(nodeInfo));
        assert  (response.equals(nodeInfo));
        fingerTable.printTable();
    }

    @Test
    public void modifyFingerTest(){
        NodeInfo nodeInfo = new NodeInfo("7000");
        fingerTable.modifyFinger(0,nodeInfo);

        //asserts

        assert (!(fingerTable.containsFinger(Utilities.computefinger("0000",0))));
        fingerTable.printTable();
    }

    @Test
    public void getFingerTest(){
        fingerTable.removeFinger(15);
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable.addFinger(nodeInfo);
        NodeInfo nodeInfo2 = fingerTable.getFinger(15);
        assert(nodeInfo.equals(nodeInfo2) );
    }

}

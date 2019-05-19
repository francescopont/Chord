package chord.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class NodeTest {
    Node node;
    NodeDispatcher mockDispatcher;

    @Before
    public void setUp() throws Exception{
        NodeInfo nodeInfo= new NodeInfo("0000");
        node= new Node(nodeInfo);

        //mock dispatcher
        mockDispatcher=mock(NodeDispatcher.class);
        node.setDispatcher(mockDispatcher);
        when(mockDispatcher.sendSuccessorRequest(new NodeInfo("0008"), "0009",nodeInfo)).thenReturn(new NodeInfo("zoro"));

        //setting FingerTable and SuccessorList
        FingerTable fingerTable= new FingerTable("0000");
        SuccessorList successorList= new SuccessorList("0000");
        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo1 = new NodeInfo(Utilities.computefinger("0000",i));
            fingerTable.addFinger(nodeInfo1);
        }
        for (int i=0; i<4; i++){
            NodeInfo nodeInfo1= new NodeInfo(Utilities.computefinger("0000", i));
            successorList.addEntry(nodeInfo1);
        }

        node.setFingerTable(fingerTable);
        node.setSuccessorList(successorList);

        fingerTable.printTable();
        successorList.printTable();
    }


    @Test
    public void stabilize() {
    }

    @Test
    public void fix_finger() {
    }

    @Test
    public void check_predecessor() {
    }

    @Test
    public void find_successor() {

        NodeInfo response= node.find_successor("0000");
        NodeInfo response1= node.find_successor("0002");
        NodeInfo response2= node.find_successor("0003");
        NodeInfo response3= node.find_successor("0007");
        NodeInfo response4= node.find_successor("0009");



        //asserts
        assert (response.getHash().equals("0000"));
        assert (response1.getHash().equals("0002"));
        assert (response2.getHash().equals("0004"));
        assert (response3.getHash().equals("0008"));
        assert (response4.getHash().equals("zoro"));
    }

    @Test
    public void initialize() {
        node.initialize();

        for(NodeInfo nodeInfo: node.getSuccessorList().getSuccessors()){
            assert (nodeInfo.equals(node.getNodeInfo()));
        }
        for(NodeInfo nodeInfo: node.getFingerTable().getFingers()){
            assert (nodeInfo.equals(node.getNodeInfo()));
        }
        assert(node.getPredecessor().equals(node.getNodeInfo()));
    }

    @Test
    public void isInitialized() {
    }

    @Test
    public void initialize1() {


    }


}
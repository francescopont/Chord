package chord.model;

import chord.Exceptions.PredecessorException;
import org.junit.Before;
import org.junit.Test;


//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;

public class NodeTest {
    Node node;
    NodeDispatcher mockDispatcher;
    Node node1;
    NodeDispatcher mockDispatcher1;

    @Before
    public void setUp() throws Exception{
        NodeInfo nodeInfo= new NodeInfo("0000");
        NodeInfo nodeInfo1 = new NodeInfo("aaaa");
        node= new Node(nodeInfo);
        node1 = new Node(nodeInfo1);


        //mockDispatcher=mock(NodeDispatcher.class);
        //mockDispatcher1 = mock(NodeDispatcher.class);
        node.setDispatcher(mockDispatcher);
        node1.setDispatcher(mockDispatcher1);
        //when(mockDispatcher.sendSuccessorRequest(new NodeInfo("0008"), "0009",nodeInfo)).thenReturn(new NodeInfo("zoro"));
        //when (mockDispatcher1.sendSuccessorRequest(nodeInfo, "aaaa", nodeInfo1)).thenReturn(node.findSuccessor("aaaa"));
        //when (mockDispatcher1.sendFirstSuccessorRequest(nodeInfo, nodeInfo1)).thenReturn(node.getFirstSuccessor());
        //setting FingerTable and SuccessorList
        FingerTable fingerTable= new FingerTable("0000");
        SuccessorList successorList= new SuccessorList("0000");
        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo2 = new NodeInfo(Utilities.computefinger("0000",i));
            fingerTable.addFinger(nodeInfo2);
        }
        for (int i=0; i<4; i++){
            NodeInfo nodeInfo2= new NodeInfo(Utilities.computefinger("0000", i));
            successorList.addEntry(nodeInfo2);
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

        NodeInfo response= node.findSuccessor("0000");
        NodeInfo response1= node.findSuccessor("0002");
        NodeInfo response2= node.findSuccessor("0003");
        NodeInfo response3= node.findSuccessor("0007");
        NodeInfo response4= node.findSuccessor("0009");



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
        try {
            NodeInfo nodeInfo = node.getPredecessor();
            assert(nodeInfo.equals(node.getNodeInfo()));
        } catch (PredecessorException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void isInitialized() {
    }

    @Test
    public void initialize1() {


    }


}
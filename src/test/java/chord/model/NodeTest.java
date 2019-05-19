package chord.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class NodeTest {
    Node node;

    @Before
    public void setUp() throws Exception{
        NodeInfo nodeInfo= new NodeInfo("0000");
        node= new Node(nodeInfo);
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
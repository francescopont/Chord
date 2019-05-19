package chord.model;

import chord.Exceptions.SuccessorListException;
import org.junit.Before;
import org.junit.Test;

import java.util.NoSuchElementException;

import static org.junit.Assert.assertTrue;

public class SuccessorListTest {
    SuccessorList successorList;
    SuccessorList successorList1;
    SuccessorList successorList2;
    SuccessorList successorList3;

    @Before
    public void setUp() throws Exception {
        this.successorList = new SuccessorList("0000");
        this.successorList1 = new SuccessorList("aaaa");
        this.successorList2 = new SuccessorList("bbbb");
        this.successorList3 = new SuccessorList("1000");

        //initializing the first variable
        for (int i=0; i<4; i++){
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger("0000", i));
            successorList.addEntry(nodeInfo);
        }

        //initializing the second variable
        for ( int i=0; i<4; i++){
            successorList1.addEntry(new NodeInfo("aaaa"));
        }

        //the third variable is left uninitialized

        //initializing the fourth variable
        for (int i=0; i<4; i++){
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger("1000", i));
            successorList.addEntry(nodeInfo);
        }
    }

    @Test
    public void addEntryTest() {
        NodeInfo nodeInfo = new NodeInfo("eeee");
        successorList.addEntry(nodeInfo);

        //asserts
        assert (!successorList.containsSuccessor("eeee"));
        assert (successorList.getSuccessors().size()== 4);
        assert (successorList.getNode().equals("0000"));
        assert (successorList1.getMap().size() == 0);
        for (int i=0; i<4; i++){
            NodeInfo nodeInfo1 = new NodeInfo(Utilities.computefinger("0000", i));
            assert (successorList.getSuccessors().get(i).equals(nodeInfo1));
        }

        //asserts
        assert (successorList1.getSuccessors().size() == 4);
        assert (successorList1.getNode().equals("aaaa"));
        assert (successorList1.getMap().size() == 0);
        for ( int i=0; i<4; i++){
            assert (successorList1.getSuccessors().get(i).equals(new NodeInfo("aaaa")));
        }

        assert (successorList2.getSuccessors().size()==0);
        successorList2.addEntry(new NodeInfo("bbbc"));
        assert (successorList2.containsSuccessor("bbbc"));
        assert (successorList2.getSuccessors().size()==1);
    }

    @Test
    public void getFirstTest() {
        NodeInfo nodeInfo = new NodeInfo("0001");
        NodeInfo nodeInfo1 = new NodeInfo("aaaa");
        boolean nullpointer = false;
        try{
            successorList2.getFirstElement();
        }catch (NoSuchElementException e){
            nullpointer = true;
        }

        //asserts
        assert (successorList.getFirstElement().equals(nodeInfo));

        //asserts
        assert (successorList1.getFirstElement().equals(nodeInfo1));

        //asserts
        assertTrue(nullpointer);

    }

    @Test
    public void getLastElement() {
        successorList2.getSuccessors().add(new NodeInfo("eeee"));

        //asserts
        assert (successorList.getLastElement().equals(new NodeInfo("8000")));
        successorList.getSuccessors().pollLast();
        NodeInfo nodeInfo = successorList.getSuccessors().getLast();
        assert (successorList.getLastElement().equals(nodeInfo));

        //asserts
        assert (successorList1.getLastElement().equals(new NodeInfo("aaaa")));

        //asserts
        assert (successorList2.getLastElement().equals(new NodeInfo("eeee")));
    }

    @Test
    public void modifyEntryTest() {
        NodeInfo nodeInfo = new NodeInfo("5000");
        successorList.modifyEntry(0,nodeInfo);

        //asserts
        //all the rest of the table must not change
        for (int i=1; i<4; i++){
            NodeInfo nodeInfo1 = new NodeInfo(Utilities.computefinger("0000", i));
            assert (successorList.getSuccessors().get(i).equals(nodeInfo1));
        }
        assert (successorList.getSuccessors().getFirst().equals(nodeInfo));
    }

    @Test
    public void closestSuccessorTest() {
        for ( int i=0; i<4; i++){
            successorList3.addEntry(new NodeInfo("eeee"));
        }
        NodeInfo me = new NodeInfo("0000");
        NodeInfo nodeInfo = new NodeInfo("0001");
        NodeInfo nodeInfo1 = new NodeInfo("0002");
        NodeInfo nodeInfo2 = new NodeInfo("0004");
        NodeInfo nodeInfo3 = new NodeInfo("0008");
        NodeInfo nodeInfo4 = new NodeInfo("0001");
        NodeInfo nodeInfo5 = new NodeInfo("aaaa");

        //asserts on the fly
        NodeInfo response = null;
        //I look for my successor
        try{
            response = successorList.closestSuccessor("0001");
        }catch (SuccessorListException e){
            //do nothing
        }
        assert (response.equals(nodeInfo));

        // I look for another random variable
        try{
            response = successorList.closestSuccessor("0005");
        }catch (SuccessorListException e){
            //do nothing
        }
        assert (response.equals(nodeInfo3));

        // I look for a key which is beyond the last entry
        boolean failed = false;
        try{
            response = successorList.closestSuccessor("0010");
        }catch (SuccessorListException e){
            failed = true;
        }
        assertTrue(failed);

        //I look for me
        try{
            response = successorList.closestSuccessor("0002");
        }catch (SuccessorListException e){
            failed = true;
        }
        assert (response.equals(nodeInfo1));

        //I look for the last entry
        try{
            response = successorList.closestSuccessor("0004");
        }catch (SuccessorListException e){
            failed = true;
        }
        assert (response.equals(nodeInfo2));

        //i check the correct behaviour with the zero
        try{
            response = successorList.closestSuccessor("9999");
        }catch (SuccessorListException e){
            failed = true;
        }
        assert (response.equals(nodeInfo4));

        //I look for me
        try{
            response = successorList1.closestSuccessor("aaaa");
        }catch (SuccessorListException e){
            failed = true;
        }
        assert (response.equals(nodeInfo5));

        try{
            response = successorList3.closestSuccessor("eeea");
        }catch (SuccessorListException e){
            failed = true;
        }
        assert(response.equals(new NodeInfo("eeee")));






    }




    @Test
    public void getElement() {
    }


    @Test
    public void completeTest(){
        NodeInfo nodeInfo = new NodeInfo("1000");
        successorList.modifyEntry(0,nodeInfo);


        NodeInfo response = null;
        try {
            response = successorList.closestSuccessor("0100");
        } catch (SuccessorListException e) {
            //do nothing
        }

        //asserts
        assert (response.equals(nodeInfo));



    }
}
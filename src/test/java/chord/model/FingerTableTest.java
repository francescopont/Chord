package chord.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Null;

public class FingerTableTest {
    FingerTable fingerTable;
    FingerTable fingerTable1;
    FingerTable fingerTable2;

    @Before
    public void setUp() throws Exception {
        String hash = "0000";
        fingerTable = new FingerTable(hash);
        fingerTable1= new FingerTable(hash);
        fingerTable2= new FingerTable(hash);

        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo = new NodeInfo(Utilities.computefinger(hash,i));
            fingerTable.addFinger(nodeInfo);
        }

        for(int i=0;i< 16; i++){
            NodeInfo nodeInfo= new NodeInfo("0000");
            fingerTable1.addFinger(nodeInfo);
        }

        for(int i=0; i<16; i++){
            NodeInfo nodeInfo=new NodeInfo("4000");
            fingerTable2.addFinger(nodeInfo);
        }


        fingerTable.printTable();
    }



    @Test
    public void addFingerTest(){
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable.addFinger(nodeInfo);

        //asserts
        assert (!fingerTable.containsFinger(nodeInfo.getHash()));
        assert (fingerTable.getFingers().size()==16);
        assert (fingerTable.getNode().equals("0000"));
        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo1 = new NodeInfo(Utilities.computefinger("0000",i));
            assert (fingerTable.getFingers().get(i).equals(nodeInfo1));
        }

        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo1 = new NodeInfo("0000");
            assert (fingerTable1.getFingers().get(i).equals(nodeInfo1));
        }

        for (int i = 0; i< 16; i++){
            //we use here the fake constructor
            NodeInfo nodeInfo1 = new NodeInfo("4000");
            assert (fingerTable2.getFingers().get(i).equals(nodeInfo1));
        }

    }

    @Test
    public void closestPredecessorTest(){

        Boolean failed=false;
        NodeInfo nodeInfo= new NodeInfo("0000");
        NodeInfo nodeInfo1= new NodeInfo("7000");
        NodeInfo nodeInfo2 = new NodeInfo("5000");
        NodeInfo nodeInfo3= new NodeInfo("0001");
        NodeInfo nodeInfo4= new NodeInfo("8000");
        NodeInfo nodeInfo5= new NodeInfo("0100");
        NodeInfo nodeInfo6= new NodeInfo("9000");

        //normal fingerTable
        NodeInfo response = fingerTable.closestPredecessor(nodeInfo.getHash());
        NodeInfo response1= fingerTable.closestPredecessor(nodeInfo1.getHash());
        NodeInfo response2= fingerTable.closestPredecessor(nodeInfo2.getHash());
        try {
            NodeInfo response3 = fingerTable.closestPredecessor(nodeInfo3.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        NodeInfo response4= fingerTable.closestPredecessor(nodeInfo4.getHash());
        NodeInfo response5= fingerTable.closestPredecessor(nodeInfo5.getHash());
        NodeInfo response6= fingerTable.closestPredecessor(nodeInfo6.getHash());
        //asserts
        assert  (response.getHash().equals("8000"));
        assert  (response1.getHash().equals("4000"));
        assert  (response2.getHash().equals("4000"));
        assert  (response4.getHash().equals("4000"));
        assert  (response5.getHash().equals("0080"));
        assert  (response6.getHash().equals("8000"));


        //fingerTable with only me
        failed=false;
        try {
            NodeInfo response7 = fingerTable1.closestPredecessor(nodeInfo.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        failed=false;
        try {
            NodeInfo response8 = fingerTable1.closestPredecessor(nodeInfo1.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        failed= false;
        try {
            NodeInfo response9 = fingerTable1.closestPredecessor(nodeInfo2.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        failed=false;
        try {
            NodeInfo response10 = fingerTable1.closestPredecessor(nodeInfo3.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        //asserts


        //fingerTable with only other
        NodeInfo response11= fingerTable2.closestPredecessor(nodeInfo.getHash());
        NodeInfo response12=fingerTable2.closestPredecessor(nodeInfo1.getHash());
        NodeInfo response13=fingerTable2.closestPredecessor(nodeInfo2.getHash());
        failed=false;
        try {
            NodeInfo response14 = fingerTable2.closestPredecessor(nodeInfo3.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        NodeInfo response15= fingerTable2.closestPredecessor(nodeInfo4.getHash());
        failed=false;
        try {
            NodeInfo response16 = fingerTable2.closestPredecessor(nodeInfo5.getHash());
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        NodeInfo response17= fingerTable2.closestPredecessor(nodeInfo6.getHash());
        //asserts
        assert  (response11.getHash().equals("4000"));
        assert  (response12.getHash().equals("4000"));
        assert  (response13.getHash().equals("4000"));
        assert  (response15.getHash().equals("4000"));
        assert  (response17.getHash().equals("4000"));

        fingerTable.printTable();
        fingerTable1.printTable();
        fingerTable2.printTable();
    }

    @Test
    public void modifyFingerTest(){
        NodeInfo nodeInfo = new NodeInfo("7000");
        fingerTable.modifyFinger(0,nodeInfo);
        fingerTable1.modifyFinger(5,nodeInfo);
        fingerTable2.modifyFinger(15,nodeInfo);

        //asserts
        assert (!(fingerTable.containsFinger(Utilities.computefinger("0000",0))));
        assert (fingerTable.getFingers().get(0).getHash().equals("7000"));
        assert (fingerTable1.getFingers().get(5).getHash().equals("7000"));
        for(int i=0;i<5;i++){
           assert (fingerTable1.getFingers().get(i).getHash().equals("0000"));
        }
        for(int i=6;i<16;i++){
            assert (fingerTable1.getFingers().get(i).getHash().equals("0000"));
        }
        assert (fingerTable2.getFingers().get(15).getHash().equals("7000"));
        for(int i=0;i<15;i++){
            assert (fingerTable2.getFingers().get(i).getHash().equals("4000"));
        }

        fingerTable.printTable();
        fingerTable1.printTable();
        fingerTable2.printTable();
    }

    @Test
    public void getFingerTest(){
        fingerTable.removeFinger(15);
        NodeInfo nodeInfo = new NodeInfo("eeee");
        fingerTable.addFinger(nodeInfo);
        NodeInfo nodeInfo2 = fingerTable.getFinger(15);
        assert(nodeInfo.equals(nodeInfo2) );
    }

    @Test
    public void completeTest(){
        Boolean failed=false;

        //modify 1 finger
        NodeInfo nodeInfo= new NodeInfo("3000");
        fingerTable.modifyFinger(6,nodeInfo);
        NodeInfo response= fingerTable.closestPredecessor("3100");
        NodeInfo response1=fingerTable.closestPredecessor("2000");
        NodeInfo response2=fingerTable.closestPredecessor("8345");
        NodeInfo response3= fingerTable.closestPredecessor("4100");
        //asserts
        assert (response.getHash().equals("3000"));
        assert (response1.getHash().equals("1000"));
        assert (response2.getHash().equals("8000"));
        assert (response3.getHash().equals("4000"));

        //print
        fingerTable.printTable();
        fingerTable1.printTable();
        fingerTable2.printTable();

        //modify more fingers
        fingerTable1.modifyFinger(0,nodeInfo);
        NodeInfo response4=fingerTable1.closestPredecessor("7000");
        try{
            NodeInfo response5= fingerTable1.closestPredecessor("3000");
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert (failed);
        failed=false;
        try{
            NodeInfo response6= fingerTable1.closestPredecessor("0010");
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        NodeInfo nodeInfo1= new NodeInfo("3100");
        for(int i=4; i<12;i++){
            fingerTable1.modifyFinger(i,nodeInfo1);
        }

        NodeInfo response7= fingerTable1.closestPredecessor("3500");
        NodeInfo response8= fingerTable1.closestPredecessor("0000");
        NodeInfo response9= fingerTable1.closestPredecessor("3010");
        failed=false;
        try{
            NodeInfo response10= fingerTable1.closestPredecessor("3000");
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        failed=false;
        try{
            NodeInfo response11= fingerTable1.closestPredecessor("1000");
        }
        catch (NullPointerException e){
            failed=true;
        }
        assert  (failed);
        //asserts
        assert (response4.getHash().equals("3000"));
        assert (response7.getHash().equals("3100"));
        assert (response8.getHash().equals("3100"));
        assert (response9.getHash().equals("3000"));

        //print
        fingerTable.printTable();
        fingerTable1.printTable();
        fingerTable2.printTable();

    }

}

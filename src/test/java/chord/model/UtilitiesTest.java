package chord.model;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import static org.junit.Assert.*;

public class UtilitiesTest {
    private Utilities utilities;

    @Before
    public void before() throws Exception {
        Node node = new Node(new NodeInfo("0000"));
        //tested class
        utilities = new Utilities(node);
    }

    @Test
    public void run() {
        assertTrue(true);
        // not tested yet

    }

    @Test
    public void hashfunction() {
        String key = "prova10&";
        String hash = Utilities.hashfunction(key);
        assertEquals(4, hash.length());
    }

    @Test
    public void computefinger() {
        String hash = "0000";

        String finger_0 = Utilities.computefinger(hash,0);
        String finger_1 = Utilities.computefinger(hash,1);
        String finger_2 = Utilities.computefinger(hash,2);
        String finger_3 = Utilities.computefinger(hash,3);
        String finger_4 = Utilities.computefinger(hash,4);
        String finger_5 = Utilities.computefinger(hash,5);
        String finger_6 = Utilities.computefinger(hash,6);
        String finger_7 = Utilities.computefinger(hash,7);
        String finger_8 = Utilities.computefinger(hash,8);
        String finger_9 = Utilities.computefinger(hash,9);
        String finger_10 = Utilities.computefinger(hash,10);
        String finger_11 = Utilities.computefinger(hash,11);
        String finger_12 = Utilities.computefinger(hash,12);
        String finger_13 = Utilities.computefinger(hash,13);
        String finger_14 = Utilities.computefinger(hash,14);
        String finger_15 = Utilities.computefinger(hash,15);



        assertEquals("0001", finger_0);
        assertEquals("0002", finger_1);
        assertEquals("0004", finger_2);
        assertEquals("0008", finger_3);
        assertEquals("0010", finger_4);
        assertEquals("0020", finger_5);
        assertEquals("0040", finger_6);
        assertEquals("0080", finger_7);
        assertEquals("0100", finger_8);
        assertEquals("0200", finger_9);
        assertEquals("0400", finger_10);
        assertEquals("0800", finger_11);
        assertEquals("1000", finger_12);
        assertEquals("2000", finger_13);
        assertEquals("4000", finger_14);
        assertEquals("8000", finger_15);
    }
}
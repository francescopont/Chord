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

        assertEquals("0001", finger_0);
    }
}
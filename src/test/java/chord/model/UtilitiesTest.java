package chord.model;

import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


import static org.junit.Assert.*;

public class UtilitiesTest {
    private Utilities utilities;
    private Node mocknode;

    @Before
    public void before() throws Exception {
        mocknode = mock(Node.class);

        //tested class
        utilities = new Utilities(mocknode);
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
        String hash = "aaaa";

        String finger_0 = Utilities.computefinger(hash,1);
        assertEquals("aaab", finger_0);
    }
}
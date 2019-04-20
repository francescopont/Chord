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
                utilities = new Utilities(mocknode);

    }

    @Test
    public void run() {
    }

    @Test
    public void hashfunction() {
    }

    @Test
    public void computefinger() {
    }
}
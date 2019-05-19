package chord.model;

import org.junit.Before;
import org.junit.Test;

public class FingerTableComparatorTest{
    FingerTableComparator comparator;
    FingerTableComparator comparator1;

    @Before
    public void setUp(){
        comparator = new FingerTableComparator("0000");
        comparator1 = new FingerTableComparator("a000");
    }
    @Test
    public void compare() {

        //asserts
        assert (comparator.compare("eeee", "0000")== -1);
        assert (comparator.compare("1000", "2000")== -1);
        assert (comparator.compare("1000", "1000") == 0);
        assert (comparator.compare("2000", "1000") == 1);
        assert (comparator.compare("0000", "0000") == 0);

        //asserts
        assert (comparator1.compare("1000", "e000")== 1);
        assert (comparator1.compare("e000", "1000") == -1);
        assert (comparator1.compare("1000", "2000")== -1);
        assert (comparator1.compare("a001", "a002")== -1);
    }
}
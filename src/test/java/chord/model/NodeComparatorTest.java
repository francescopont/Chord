package chord.model;

import org.junit.Test;

public class NodeComparatorTest {
    NodeComparator comparator;
    NodeComparator comparator1;

    @Test
    public void compare() {
        comparator = new NodeComparator("0000");
        comparator1 = new NodeComparator("a000");

        //asserts
        assert (comparator.compare("eeee", "0000")== 1);
        assert (comparator.compare("0000", "0001")== -1);
        assert (comparator.compare("1000", "2000")== -1);
        assert (comparator.compare("1000", "1000") == 0);
        assert (comparator.compare("2000", "1000") == 1);
        assert (comparator.compare("0000", "0000") == 0);

        //asserts
        assert (comparator1.compare("1000", "e000")== 1);
        assert (comparator1.compare("e000", "1000") == -1);
        assert (comparator1.compare("1000", "2000")== -1);
        assert (comparator1.compare("a001", "a002")== -1);
        assert (comparator1.compare("9999", "a000")== 1);
    }
}
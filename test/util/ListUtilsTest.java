package util;

import org.junit.Test;
import play.test.UnitTest;

import java.util.List;

public class ListUtilsTest extends UnitTest {
    @Test
    public void testCreateListWithNumbersFromTo() throws Exception {
        final List<Integer> list1 = ListUtils.createListWithNumbersFromTo(0, 0);
        assertEquals(1, list1.size());
        assertEquals(new Long(0), new Long(list1.get(0)));

        final List<Integer> list2 = ListUtils.createListWithNumbersFromTo(0, 1);
        assertEquals(2, list2.size());
        assertEquals(new Long(0), new Long(list2.get(0)));
        assertEquals(new Long(1), new Long(list2.get(1)));

        final List<Integer> list3 = ListUtils.createListWithNumbersFromTo(2, 5);
        assertEquals(4, list3.size());
        assertEquals(new Long(2), new Long(list3.get(0)));
        assertEquals(new Long(3), new Long(list3.get(1)));
        assertEquals(new Long(4), new Long(list3.get(2)));
        assertEquals(new Long(5), new Long(list3.get(3)));

        final List<Integer> list4 = ListUtils.createListWithNumbersFromTo(5, 2);
        assertEquals(4, list4.size());
        assertEquals(new Long(5), new Long(list4.get(0)));
        assertEquals(new Long(4), new Long(list4.get(1)));
        assertEquals(new Long(3), new Long(list4.get(2)));
        assertEquals(new Long(2), new Long(list4.get(3)));

        final List<Integer> list5 = ListUtils.createListWithNumbersFromTo(-4, 5);
        assertEquals(10, list5.size());
        assertEquals(new Long(-4), new Long(list5.get(0)));
        assertEquals(new Long(5), new Long(list5.get(9)));
        final List<Integer> list6 = ListUtils.createListWithNumbersFromTo(5, -4);
        assertEquals(10, list6.size());
        assertEquals(new Long(-4), new Long(list6.get(9)));
        assertEquals(new Long(5), new Long(list6.get(0)));
    }
}

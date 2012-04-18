package models;

import org.junit.Test;
import play.test.UnitTest;
import util.ListUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CouponTest extends UnitTest {
    @Test
    public void numberGeneration() {
        Coupon coupon = new Coupon();
        final List<Integer> numbersAsList = coupon.getNumbersAsList();
        assertEquals("24 numbers in coupon", 24, numbersAsList.size());
        final Set<Integer> set = new HashSet<Integer>();
        set.addAll(numbersAsList);
        assertEquals("24 different numbers in coupon", 24, set.size());
        final int markedNumber = numbersAsList.get(2);
        coupon.setMarked(markedNumber, true);
        assertTrue("marked number should be marked", coupon.getMarked(markedNumber));
        assertFalse("not yet marked number should be unmarked", coupon.getMarked(numbersAsList.get(3)));
        coupon.save();
        final long id = coupon.getId();
        coupon = null;
        coupon = Coupon.findById(id);
        assertEquals("save operation does not affect sequential arrangement", numbersAsList, coupon.getNumbersAsList());
        assertTrue("marked number should be marked", coupon.getMarked(markedNumber));
        assertFalse("not yet marked number should be unmarked", coupon.getMarked(numbersAsList.get(3)));
    }

    @Test
    public void invalidNumberMarked() {
        Coupon coupon = new Coupon();
        final List<Integer> numbersInCoupon = coupon.getNumbersAsList();
        final List<Integer> possibleNumbers = ListUtils.createListWithNumbersFromTo(1, BingoGame.NUMBERS_IN_GAME);
        final List<Integer> numbersNotInCoupon = org.apache.commons.collections.ListUtils.subtract(possibleNumbers, numbersInCoupon);
        final Integer numberNotInCoupon = numbersNotInCoupon.get(0);
        coupon.setMarked(numberNotInCoupon, true);
        assertFalse(coupon.getMarked(numberNotInCoupon));
    }
}

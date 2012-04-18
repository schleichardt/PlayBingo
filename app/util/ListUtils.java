package util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
    public static List<Integer> createListWithNumbersFromTo(Integer from, Integer to) {
        final int size = Math.abs(Math.max(to, from) - Math.min(from, to) + 1);
        List<Integer> list = new ArrayList<Integer>(size);
        if (to > from) {
            for (int i = 0; i < size; i++) {
                list.add(i + from);
            }
        } else {
            for (int i = 0; i < size; i++) {
                list.add(from - i);
            }
        }
        return list;
    }
}

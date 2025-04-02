package zzzank.probejs.utils;

import lombok.val;

/**
 * @author ZZZank
 */
public interface Asser {
    static void t(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }

    static <T> T tNotNull(T value, String name) {
        if (value == null) {
            throw new NullPointerException("'" + name + "' must not be null");
        }
        return value;
    }

    static <T extends Iterable<?>> T tNotNullAll(T collection, String name) {
        tNotNull(collection, name);
        val elementName = "element in " + name;
        for (val o : collection) {
            tNotNull(o, elementName);
        }
        return collection;
    }

    static <T> T[] tNotNullAll(T[] collection, String name) {
        tNotNull(collection, name);
        val elementName = "element in " + name;
        for (val o : collection) {
            tNotNull(o, elementName);
        }
        return collection;
    }
}

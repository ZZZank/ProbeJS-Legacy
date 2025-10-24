package zzzank.probejs.utils;

import lombok.val;
import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public interface Asser {
    static void t(boolean condition, String errorMessage) {
        if (!condition) {
            throw new AssertionError(errorMessage);
        }
    }

    static String nullMessage(String name) {
        return "'" + name + "' must not be null";
    }

    @NotNull
    static <T> T tNotNull(T value, String name) {
        if (value == null) {
            throw new NullPointerException(nullMessage(name));
        }
        return value;
    }

    @NotNull
    static <T extends Iterable<?>> T tNotNullAll(T collection, String name) {
        tNotNull(collection, name);
        val elementName = "element in " + name;
        for (val o : collection) {
            tNotNull(o, elementName);
        }
        return collection;
    }

    @NotNull
    static <T> T[] tNotNullAll(T[] collection, String name) {
        tNotNull(collection, name);
        val elementName = "element in " + name;
        for (val o : collection) {
            tNotNull(o, elementName);
        }
        return collection;
    }
}

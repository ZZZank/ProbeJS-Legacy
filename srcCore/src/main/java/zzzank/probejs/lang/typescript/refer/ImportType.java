package zzzank.probejs.lang.typescript.refer;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.UnaryOperator;

/**
 * @author ZZZank
 */
public class ImportType implements Comparable<ImportType> {
    private static final Map<String, ImportType> REGISTERED = new HashMap<>();
    private static final List<ImportType> INDEXED = new ArrayList<>();
    private static int currentIndex = 0;

    public static ImportType register(String name, UnaryOperator<String> formatter) {
        if (REGISTERED.containsKey(name)) {
            throw new IllegalArgumentException("key %s already existed");
        }
        val registered = new ImportType(name, currentIndex++, formatter);
        REGISTERED.put(name, registered);
        INDEXED.add(registered);
        return registered;
    }

    public static final ImportType ORIGINAL = register("original", UnaryOperator.identity());
    public static final ImportType STATIC = register("static", s -> s + "$$Static");
    public static final ImportType TYPE = register("type", s -> s + "$$Type");

    public static final List<ImportType> ALL = Collections.unmodifiableList(INDEXED);

    public final String name;
    public final int ordinal;
    public final UnaryOperator<String> formatter;

    private ImportType(String name, int ordinal, UnaryOperator<String> formatter) {
        this.name = Objects.requireNonNull(name);
        this.ordinal = ordinal;
        this.formatter = Objects.requireNonNull(formatter);
    }

    public String fmt(String s) {
        return formatter.apply(s);
    }

    @Override
    public int compareTo(@NotNull ImportType o) {
        return Integer.compare(ordinal, o.ordinal);
    }
}

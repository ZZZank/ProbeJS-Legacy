package zzzank.probejs.lang.parchment.data;

import org.objectweb.asm.Type;

import java.util.Objects;

/**
 * @author ZZZank
 */
public class PrefixDiff {
    public static final PrefixDiff EMPTY = new PrefixDiff(new String[0]);

    public static PrefixDiff fromString(String diff) {
        return new PrefixDiff(diff.split("/"));
    }

    private final String[] parts;

    private PrefixDiff(String[] parts) {
        this.parts = parts;
    }

    static String diffFriendlyName(Type type) {
        return type.getInternalName();
    }

    public PrefixDiff toDiff(String value) {
        var split = value.split("/");

        var len = Math.min(parts.length, split.length);
        for (int i = 0; i < len; i++) {
            if (Objects.equals(parts[i], split[i])) {
                split[i] = "";
            }
        }

        return new PrefixDiff(split);
    }

    public String restore(PrefixDiff diff) {
        var builder = new StringBuilder();

        var len = diff.parts.length;
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                builder.append('/');
            }
            var part = diff.parts[i];
            builder.append(part.isEmpty() ? this.parts[i] : part);
        }

        return builder.toString();
    }

    @Override
    public String toString() {
        return String.join("/", parts);
    }
}

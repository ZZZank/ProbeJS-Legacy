package zzzank.probejs.features.parchment.data;

import java.util.Objects;

/**
 * @author ZZZank
 */
public class Diffable {

    public static Diffable of(String string, char split) {
        return new Diffable(string.split(String.valueOf(split)), split);
    }

    private final String[] parts;
    public final char splitBy;

    private Diffable(String[] parts, char split) {
        this.parts = parts;
        this.splitBy = split;
    }

    public String toDiff(Diffable value) {
        var split = value.parts.clone();

        var len = Math.min(parts.length, split.length);
        for (int i = 0; i < len; i++) {
            if (Objects.equals(parts[i], split[i])) {
                split[i] = "";
            }
        }

        return String.join(String.valueOf(this.splitBy), split);
    }

    public Diffable restore(String diff) {
        var split = diff.split(String.valueOf(this.splitBy));

        for (int i = 0; i < split.length; i++) {
            if (split[i].isEmpty() && i < this.parts.length) {
                split[i] = this.parts[i];
            }
        }

        return new Diffable(split, this.splitBy);
    }

    @Override
    public String toString() {
        return String.join("/", parts);
    }
}

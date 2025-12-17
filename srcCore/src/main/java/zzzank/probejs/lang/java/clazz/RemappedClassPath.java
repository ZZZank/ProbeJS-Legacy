package zzzank.probejs.lang.java.clazz;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.utils.CollectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * @author ZZZank
 */
public final class RemappedClassPath implements Comparable<RemappedClassPath> {
    public static final RemappedClassPath EMPTY = new RemappedClassPath("", new String[0]);

    public static RemappedClassPath ofArtificial(String name) {
        return new RemappedClassPath("", name.split("\\."));
    }

    public static RemappedClassPath ofJava(Class<?> c) {
        var remapped = RemapperBridge.remapClass(c);
        return new RemappedClassPath(c.getName(), remapped.split("\\."));
    }

    private final String className;
    private final String[] remapped;

    private RemappedClassPath(String className, String[] remapped) {
        this.className = className;
        this.remapped = remapped;
    }

    public List<String> viewParts() {
        return Collections.unmodifiableList(Arrays.asList(remapped));
    }

    public String getPart(int index) {
        return this.remapped[index];
    }

    public int countParts() {
        return this.remapped.length;
    }

    public List<String> viewPackage() {
        return viewParts().subList(0, this.remapped.length - 1);
    }

    public String getRemappedName() {
        return String.join(".", this.remapped);
    }

    /// @return The full class name if this class path represents a Java class, or an empty string if this class path is artificial.
    /// @see Class#getName()
    /// @see #getFirstValidPath()
    public String getOriginalName() {
        return this.className;
    }

    /// [#getOriginalName()] if its result is not empty, [#getRemappedName()] otherwise
    public String getFirstValidPath() {
        return this.className.isEmpty() ? getRemappedName() : this.className;
    }

    public String getSimpleName() {
        return this.remapped[this.remapped.length - 1];
    }

    public boolean isArtificial() {
        return this.className.isEmpty();
    }

    @Override
    public int compareTo(@NotNull RemappedClassPath other) {
        return Arrays.compare(this.remapped, other.remapped);
    }

    public String toDiff(final @NotNull RemappedClassPath base) {
        var commonPartsCount = CollectUtils.countCommonPrefix(this.remapped, remapped);
        var diff = this.remapped.clone();
        Arrays.fill(diff, 0, commonPartsCount, "");
        return String.join(".", diff);
    }

    public RemappedClassPath fromDiff(String diff, UnaryOperator<String> unmapper) {
        var remapped = diff.split("\\.");
        for (int i = 0; i < remapped.length; i++) {
            if (remapped[i].isEmpty()) {
                remapped[i] = this.remapped[i];
            } else {
                break;
            }
        }
        return new RemappedClassPath(unmapper.apply(String.join(".", remapped)), remapped);
    }

    public boolean equals(final Object o) {
        return o instanceof RemappedClassPath other && Objects.equals(this.className, other.className);
    }

    public int hashCode() {
        return className.hashCode();
    }
}

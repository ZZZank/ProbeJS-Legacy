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
public final class ClassPath implements Comparable<ClassPath> {
    public static final ClassPath EMPTY = new ClassPath("", new String[0]);

    public static ClassPath ofArtificial(String name) {
        return new ClassPath("", name.split("\\."));
    }

    public static ClassPath ofJava(Class<?> c) {
        var remapped = RemapperBridge.remapClass(c);
        return new ClassPath(c.getName(), remapped.split("\\."));
    }

    public static ClassPath ofRemapped(String remapped, UnaryOperator<String> unmapper) {
        return new ClassPath(unmapper.apply(remapped), remapped.split("\\."));
    }

    private final String className;
    private final String[] remapped;

    private ClassPath(String className, String[] remapped) {
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
    public int compareTo(@NotNull ClassPath other) {
        return Arrays.compare(this.remapped, other.remapped);
    }

    public String toDiff(final @NotNull ClassPath base) {
        var commonPartsCount = CollectUtils.countCommonPrefix(this.remapped, base.remapped);
        var diff = this.remapped.clone();
        Arrays.fill(diff, 0, commonPartsCount, "");
        return String.join(".", diff);
    }

    public ClassPath fromDiff(String diff, UnaryOperator<String> unmapper) {
        var remapped = diff.split("\\.");
        for (int i = 0; i < remapped.length; i++) {
            if (remapped[i].isEmpty()) {
                remapped[i] = this.remapped[i];
            } else {
                break;
            }
        }
        return new ClassPath(unmapper.apply(String.join(".", remapped)), remapped);
    }

    public boolean equals(final Object o) {
        if (o instanceof ClassPath other) {
            return isArtificial()
                ? Arrays.equals(remapped, other.remapped)
                : Objects.equals(className, other.className);
        }
        return false;
    }

    public int hashCode() {
        return isArtificial() ? Arrays.hashCode(remapped) : className.hashCode();
    }

    @Override
    public String toString() {
        if (isArtificial()) {
            return String.format("ClassPath[%s]", getRemappedName());
        } else {
            return String.format("ClassPath[%s->%s]", getOriginalName(), getRemappedName());
        }
    }
}

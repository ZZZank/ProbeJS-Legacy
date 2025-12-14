package zzzank.probejs.lang.java.clazz;

import lombok.ToString;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.utils.CollectUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@ToString
public final class ClassPath implements Comparable<ClassPath> {
    public static final ClassPath EMPTY = new ClassPath(new String[0]);

    public static final String TS_PATH_PREFIX = "packages/";

    private final String[] parts;

    private ClassPath(String[] parts) {
        this.parts = parts;
    }

    public static @NotNull ClassPath fromRaw(final String className) {
        if (className == null || className.isEmpty()) {
            throw new IllegalArgumentException("'className' is " + (className == null ? "null" : "empty"));
        }
        return new ClassPath(className.split("\\."));
    }

    public static ClassPath fromJava(final @NotNull Class<?> clazz) {
        val name = RemapperBridge.remapClass(Objects.requireNonNull(clazz));
        val parts = name.split("\\.");
        parts[parts.length - 1] = "$" + parts[parts.length - 1];
        return new ClassPath(parts);
    }

    public static ClassPath fromTS(final @NotNull String typeScriptPath) {
        if (!typeScriptPath.startsWith(TS_PATH_PREFIX)) {
            throw new IllegalArgumentException(String.format("path '%s' is not ProbeJS TS path", typeScriptPath));
        }
        val name = typeScriptPath.substring(TS_PATH_PREFIX.length());
        return new ClassPath(name.split("/"));
    }

    public String getPart(final int index) {
        return parts[index];
    }

    public List<String> getParts() {
        return Collections.unmodifiableList(Arrays.asList(this.parts));
    }

    public int getPartsCount() {
        return parts.length;
    }

    public String getName() {
        return parts[parts.length - 1];
    }

    /// @see Class#getSimpleName()
    public String getJavaName() {
        val name = getName();
        return name.startsWith("$") ? name.substring(1) : name;
    }

    public String getConcatenated(final String sep) {
        return String.join(sep, parts);
    }

    /// mapped, '.' split path with name prefix '$' for native class
    public String getDirectPath() {
        return getConcatenated(".");
    }

    /// @return mapped, `.` split path without name prefix '$' for native class
    public String getJavaStylePath() {
        val copy = CollectUtils.ofList(parts);
        val last = parts[parts.length - 1];
        if (last.startsWith("$")) {
            copy.set(parts.length - 1, last.substring(1));
        }
        return String.join(".", copy);
    }

    /// unmapped, `.` split path without name prefix '$' for native class
    /// @see Class#getName()
    public String getJavaPath() {
        return RemapperBridge.unmapClass(getJavaStylePath());
    }

    /// @return mapped, '/' split path with name prefix '$' for native class
    public String getTSPath() {
        return TS_PATH_PREFIX + getConcatenated("/");
    }

    public List<String> getPackage() {
        return getParts().subList(0, this.parts.length - 1);
    }

    public String getConcatenatedPackage(final String sep) {
        return String.join(sep, getPackage());
    }

    @Override
    public int compareTo(final @NotNull ClassPath other) {
        return Arrays.compare(this.parts, other.parts);
    }

    /// Convert `this` Classpath object to diff represented in [String].
    public String toDiff(ClassPath base) {
        var diff = this.parts.clone();
        var commonPartsCount = CollectUtils.countCommonPrefix(this.parts, base.parts);
        Arrays.fill(diff, 0, commonPartsCount, "");
        return String.join(".", diff);
    }

    /// Convert `diff` string back to ClassPath object, with `this` being the base
    public ClassPath fromDiff(String diff) {
        var parts = diff.split("\\.");
        for (var i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                parts[i] = this.parts[i];
            } else {
                break;
            }
        }
        return new ClassPath(parts);
    }

    public boolean equals(final Object o) {
        return o instanceof ClassPath other && Arrays.equals(this.parts, other.parts);
    }

    public int hashCode() {
        return Arrays.hashCode(this.parts);
    }
}

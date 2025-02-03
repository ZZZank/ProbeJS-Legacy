package zzzank.probejs.lang.typescript.refer;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.ClassPath;

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public final class ImportInfo {

    public final ClassPath path;
    public int imports;

    private ImportInfo(ClassPath path) {
        this.path = path;
        this.imports = 0;
    }

    public ImportInfo addType(@NotNull ImportType type) {
        imports |= 1 << type.ordinal;
        return this;
    }

    public ImportInfo addTypes(@NotNull ImportType... types) {
        for (val type : types) {
            addType(type);
        }
        return this;
    }

    public ImportInfo mergeWith(@NotNull ImportInfo addition) {
        imports |= addition.imports;
        return this;
    }

    public static ImportInfo of(ClassPath path, ImportType type) {
        return new ImportInfo(Objects.requireNonNull(path)).addType(type);
    }

    public static ImportInfo of(ClassPath path, ImportType... types) {
        return new ImportInfo(Objects.requireNonNull(path)).addTypes(types);
    }

    public static ImportInfo ofDefault(ClassPath path) {
        return new ImportInfo(path).addType(ImportType.ORIGINAL).addType(ImportType.TYPE);
    }

    public static ImportInfo ofType(ClassPath path) {
        return of(path, ImportType.TYPE);
    }

    public static ImportInfo ofOriginal(ClassPath path) {
        return of(path, ImportType.ORIGINAL);
    }

    public static ImportInfo ofStatic(ClassPath path) {
        return of(path, ImportType.STATIC);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImportInfo info && path.equals(info.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    public Stream<ImportType> getTypes() {
        return IntStream.range(0, ImportType.ALL.size())
            .filter(i -> ((imports >> i) & 1) != 0)
            .mapToObj(ImportType.ALL::get);
    }
}

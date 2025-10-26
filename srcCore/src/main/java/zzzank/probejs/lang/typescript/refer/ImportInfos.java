package zzzank.probejs.lang.typescript.refer;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ImportInfos implements Iterable<ImportInfo> {

    protected final Map<ClassPath, ImportInfo> raw;

    protected ImportInfos(Map<ClassPath, ImportInfo> raw) {
        this.raw = raw;
    }

    public static ImportInfos of(@NotNull ImportInfos toCopy) {
        return new ImportInfos(new HashMap<>(toCopy.raw));
    }

    public static ImportInfos of() {
        return new ImportInfos(new HashMap<>());
    }

    public static ImportInfos of(@NotNull ImportInfo info) {
        val map = new HashMap<ClassPath, ImportInfo>(CollectUtils.calcMapExpectedSize(1));
        map.put(info.path, info);
        return new ImportInfos(map);
    }

    public static ImportInfos of(@NotNull ImportInfo... initial) {
        return of(Arrays.asList(initial));
    }

    public static ImportInfos of(@NotNull Collection<ImportInfo> infos) {
        return new ImportInfos(CollectUtils.ofSizedMap(infos.size())).addAll(infos);
    }

    public static ImportInfos of(@NotNull Stream<ImportInfo> infos) {
        return new ImportInfos(new HashMap<>()).addAll(infos);
    }

    public ImportInfos add(@NotNull ImportInfo info) {
        val old = raw.put(info.path, info);
        if (old != null) {
            info.mergeWith(old);
        }
        return this;
    }

    public ImportInfos addAll(@NotNull ImportInfos other) {
        return addAll(other.getImports());
    }

    public ImportInfos addAll(@NotNull Stream<ImportInfo> infos) {
        infos.forEach(this::add);
        return this;
    }

    public ImportInfos addAll(@NotNull Collection<ImportInfo> infos) {
        for (val info : infos) {
            add(info);
        }
        return this;
    }

    public ImportInfos fromCode(Code code) {
        return code == null ? this : addAll(code.getImportInfos());
    }

    public ImportInfos fromCode(BaseType code, BaseType.FormatType type) {
        return code == null ? this : addAll(code.getImportInfos(type));
    }

    public ImportInfos fromCodes(@NotNull Stream<? extends Code> codes) {
        codes.forEach(this::fromCode);
        return this;
    }

    public ImportInfos fromCodes(@NotNull Stream<? extends BaseType> codes, BaseType.FormatType type) {
        codes.forEach(c -> this.fromCode(c, type));
        return this;
    }

    public ImportInfos fromCodes(@NotNull Collection<? extends Code> codes) {
        for (val code : codes) {
            fromCode(code);
        }
        return this;
    }

    public ImportInfos fromCodes(@NotNull Collection<? extends BaseType> codes, BaseType.FormatType type) {
        for (val code : codes) {
            fromCode(code, type);
        }
        return this;
    }

    public Collection<ImportInfo> getImports() {
        return raw.values();
    }

    public Map<ClassPath, ImportInfo> getRaw() {
        return Collections.unmodifiableMap(raw);
    }

    @Override
    public @NotNull Iterator<ImportInfo> iterator() {
        return raw.values().iterator();
    }
}

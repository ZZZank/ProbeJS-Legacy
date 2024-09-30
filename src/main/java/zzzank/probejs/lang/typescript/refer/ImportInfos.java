package zzzank.probejs.lang.typescript.refer;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public final class ImportInfos {

    public static ImportInfos of(ImportInfo... initial) {
        val infos = new ImportInfos();
        for (val info : initial) {
            infos.add(info);
        }
        return infos;
    }

    public static ImportInfos of(Collection<ImportInfo> infos) {
        return new ImportInfos().addAll(infos);
    }

    public static ImportInfos of(Stream<ImportInfo> infos) {
        return new ImportInfos().addAll(infos);
    }

    private final Map<ClassPath, ImportInfo> raw;

    private ImportInfos() {
        this.raw = new HashMap<>();
    }

    public ImportInfos add(ImportInfo info) {
        val old = raw.put(info.path, info);
        if (old != null) {
            info.types.addAll(old.types);
        }
        return this;
    }

    public ImportInfos addAll(Stream<ImportInfo> infos) {
        infos.forEach(this::add);
        return this;
    }

    public ImportInfos addAll(Collection<ImportInfo> infos) {
        for (val info : infos) {
            add(info);
        }
        return this;
    }

    public ImportInfos fromCode(Code code) {
        return addAll(code != null ? code.getImportInfos() : Collections.emptySet());
    }

    public ImportInfos fromCodes(Stream<? extends Code> codes) {
        codes.forEach(this::fromCode);
        return this;
    }

    public ImportInfos fromCodes(Collection<? extends Code> codes) {
        for (val code : codes) {
            fromCode(code);
        }
        return this;
    }

    public void pushDeclarations(@NotNull Declaration declaration) {
        Objects.requireNonNull(declaration);
        for (val info : getImports()) {
            declaration.addImport(info);
        }
    }

    public Collection<ImportInfo> getImports() {
        return raw.values();
    }

    public Map<ClassPath, ImportInfo> getRaw() {
        return Collections.unmodifiableMap(raw);
    }
}

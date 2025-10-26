package zzzank.probejs.lang.typescript.code.type.utility;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

/**
 * @author ZZZank
 */
public class ImportShield<T extends BaseType> extends BaseType {
    public final T inner;
    private final ImportInfos imports;

    public ImportShield(T inner, ImportInfos imports) {
        this.inner = inner;
        this.imports = imports;
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return imports != null ? imports : inner.getImportInfos(type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return inner.line(declaration, formatType);
    }
}

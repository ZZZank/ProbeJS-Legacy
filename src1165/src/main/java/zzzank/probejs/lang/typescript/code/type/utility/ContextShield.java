package zzzank.probejs.lang.typescript.code.type.utility;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;

public class ContextShield<T extends BaseType> extends BaseType {
    public final T inner;
    public final FormatType formatType;

    public ContextShield(T inner, FormatType formatType) {
        this.inner = inner;
        this.formatType = formatType;
    }

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return inner.getImportInfos(formatType);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return inner.line(declaration, this.formatType);
    }
}

package zzzank.probejs.lang.typescript.code.type.utility;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

public class ContextShield extends BaseType {
    public final BaseType inner;
    public final FormatType formatType;

    public ContextShield(BaseType inner, FormatType formatType) {
        this.inner = inner;
        this.formatType = formatType;
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return inner.getImportInfos(formatType);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return inner.line(declaration, this.formatType);
    }
}

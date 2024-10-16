package zzzank.probejs.lang.typescript.code.type;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.List;

public class ContextShield extends BaseType {
    private final BaseType inner;
    private final FormatType formatType;

    public ContextShield(BaseType inner, FormatType formatType) {
        this.inner = inner;
        this.formatType = formatType;
    }

    @Override
    public ImportInfos getImportInfos() {
        return inner.getImportInfos();
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return inner.format(declaration, formatType);
    }
}

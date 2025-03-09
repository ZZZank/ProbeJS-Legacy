package zzzank.probejs.lang.typescript.code.ts;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.Collections;
import java.util.List;

public class VariableDeclaration extends CommentableCode {

    public String symbol;
    public BaseType type;
    public BaseType.FormatType typeFormat = BaseType.FormatType.RETURN;

    public VariableDeclaration(String symbol, BaseType type) {
        this.symbol = symbol;
        this.type = type;
    }

    public VariableDeclaration setTypeFormat(BaseType.FormatType typeFormat) {
        this.typeFormat = typeFormat;
        return this;
    }

    @Override
    public ImportInfos getImportInfos() {
        return type.getImportInfos(typeFormat);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return Collections.singletonList(String.format("const %s: %s", symbol, type.line(declaration)));
    }
}

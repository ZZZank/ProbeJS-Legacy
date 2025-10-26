package zzzank.probejs.lang.typescript.code.ts;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;

import java.util.Collections;
import java.util.List;

public class ReexportDeclaration extends VariableDeclaration {

    public ReexportDeclaration(String symbol, BaseType type) {
        super(symbol, type);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return Collections.singletonList(String.format("export import %s = %s", symbol, type.line(declaration, BaseType.FormatType.RETURN)));
    }
}

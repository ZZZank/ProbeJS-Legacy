package zzzank.probejs.lang.typescript.code.type.js;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

@AllArgsConstructor
public class JSTypeOfType extends BaseType {

    public final BaseType inner;

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return inner.getImportInfos(type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return String.format("(typeof %s)", inner.line(declaration, FormatType.RETURN));
    }
}

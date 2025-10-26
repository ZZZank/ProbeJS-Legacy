package zzzank.probejs.lang.typescript.code.type.js;

import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

@EqualsAndHashCode(callSuper = false)
public class JSPrimitiveType extends BaseType {

    public final String content;

    public JSPrimitiveType(String content) {
        this.content = content;
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return ImportInfos.of();
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return content;
    }
}

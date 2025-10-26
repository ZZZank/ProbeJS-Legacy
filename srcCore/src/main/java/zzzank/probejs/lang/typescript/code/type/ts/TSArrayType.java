package zzzank.probejs.lang.typescript.code.type.ts;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

@AllArgsConstructor
public class TSArrayType extends BaseType {
    public BaseType component;

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return component.getImportInfos(type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return component.line(declaration, formatType) + "[]";
    }
}

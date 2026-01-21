package zzzank.probejs.lang.typescript.code.type.ts;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

/**
 * @author ZZZank
 */
public class TSArrayAccessType extends BaseType {
    public BaseType array;
    public BaseType index;

    public TSArrayAccessType(BaseType array, BaseType index) {
        this.array = array;
        this.index = index;
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return array.getImportInfos(type).addAll(index.getImportInfos(type));
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return String.format("%s[%s]", array.line(declaration, formatType), index.line(declaration, formatType));
    }
}

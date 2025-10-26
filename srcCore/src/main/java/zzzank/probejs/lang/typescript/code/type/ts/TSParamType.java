package zzzank.probejs.lang.typescript.code.type.ts;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TSParamType extends BaseType {
    public final BaseType baseType;
    public final List<BaseType> params;

    public TSParamType(BaseType baseType, @NotNull Collection<? extends BaseType> params) {
        this.baseType = baseType;
        this.params = new ArrayList<>(params);
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return ImportInfos.of(baseType.getImportInfos(type)).fromCodes(params, type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return baseType.line(declaration, formatType)
            + Types.join(", ", "<", ">", params).line(declaration, formatType);
    }
}

package zzzank.probejs.lang.typescript.code.type.ts;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

/**
 * @author ZZZank
 */
public class TSTernaryType extends BaseType {

    public String symbol;
    public BaseType extend;
    public BaseType ifTrue;
    public BaseType ifFalse;

    public TSTernaryType(String symbol, BaseType extend, BaseType ifTrue, BaseType ifFalse) {
        this.symbol = symbol;
        this.extend = extend;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return ImportInfos.of().fromCode(extend, FormatType.VARIABLE).fromCode(ifTrue).fromCode(ifFalse);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return String.format(
            "%s extends %s ? %s : %s",
            symbol,
            extend.contextShield(FormatType.VARIABLE).line(declaration, formatType),
            ifTrue.format(declaration, formatType),
            ifFalse.format(declaration, formatType)
        );
    }
}

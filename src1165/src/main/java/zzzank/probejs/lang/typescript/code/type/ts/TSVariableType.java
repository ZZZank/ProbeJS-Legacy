package zzzank.probejs.lang.typescript.code.type.ts;

import lombok.val;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;

public class TSVariableType extends BaseType {
    public String symbol;
    public BaseType extend;
    public BaseType defaultTo;

    public TSVariableType(String symbol, @Nullable BaseType extend, BaseType defaultTo) {
        this.symbol = symbol;
        this.extend = extend == Types.ANY ? null : extend;
        this.defaultTo = defaultTo;
    }

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        val imports = ImportInfos.of();
        if (extend != null) {
            imports.addAll(extend.getImportInfos(type));
        }
        if (defaultTo != null) {
            imports.addAll(defaultTo.getImportInfos(type));
        }
        return imports;
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        if (formatType != FormatType.VARIABLE) {
            return symbol;
        }
        val builder = new StringBuilder();
        //name
        builder.append(symbol);
        if (extend != null) {
            builder.append(" extends ").append(extend.line(declaration, FormatType.RETURN));
        }
        if (defaultTo != null) {
            builder.append(" = ").append(defaultTo.line(declaration, FormatType.RETURN));
        }
        return builder.toString();
    }
}

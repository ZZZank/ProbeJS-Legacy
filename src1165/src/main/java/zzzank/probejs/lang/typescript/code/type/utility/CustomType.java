package zzzank.probejs.lang.typescript.code.type.utility;

import lombok.AllArgsConstructor;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.function.Function;

@AllArgsConstructor
public class CustomType extends BaseType {
    public final BiFunction<Declaration, FormatType, String> formatter;
    public final Function<FormatType, ImportInfos> imports;

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return imports.apply(type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return formatter.apply(declaration, formatType);
    }
}

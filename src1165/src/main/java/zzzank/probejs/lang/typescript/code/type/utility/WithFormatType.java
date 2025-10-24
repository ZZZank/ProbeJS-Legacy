package zzzank.probejs.lang.typescript.code.type.utility;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import javax.annotation.Nonnull;
import java.util.Arrays;

/**
 * @author ZZZank
 */
public class WithFormatType extends BaseType {
    private final String format;
    private final BaseType[] types;

    public WithFormatType(String format, BaseType... types) {
        this.format = format;
        this.types = types;
    }

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return ImportInfos.of().fromCodes(Arrays.asList(types), type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        val additions = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            additions[i] = types[i].line(declaration, formatType);
        }
        return String.format(format, additions);
    }
}

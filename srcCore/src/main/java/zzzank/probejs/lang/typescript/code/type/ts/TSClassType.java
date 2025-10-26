package zzzank.probejs.lang.typescript.code.type.ts;

import lombok.AllArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.lang.typescript.refer.ImportType;

@AllArgsConstructor
public class TSClassType extends BaseType {
    public ClassPath classPath;

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return ImportInfos.of(switch (type) {
            case RETURN -> ImportInfo.ofOriginal(classPath);
            case INPUT -> ImportInfo.ofType(classPath);
            default -> ImportInfo.ofDefault(classPath);
        });
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        val symbol = declaration.getSymbol(classPath);
        if (formatType == FormatType.INPUT) {
            return ImportType.TYPE.fmt(symbol);
        }
        return symbol;
    }
}

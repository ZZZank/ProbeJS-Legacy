package zzzank.probejs.lang.typescript.code.type.utility;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.lang.typescript.refer.ImportType;

import javax.annotation.Nonnull;

/**
 * @author ZZZank
 */
public class StaticType extends TSClassType {
    public StaticType(ClassPath classPath) {
        super(classPath);
    }

    @Override
    public ImportInfos getImportInfos(@Nonnull FormatType type) {
        return ImportInfos.of(ImportInfo.ofStatic(classPath));
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        val name = declaration.getSymbol(classPath);
        return ImportType.STATIC.fmt(name);
    }
}

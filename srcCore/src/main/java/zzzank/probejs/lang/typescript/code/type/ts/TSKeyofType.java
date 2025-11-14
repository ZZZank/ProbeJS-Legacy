package zzzank.probejs.lang.typescript.code.type.ts;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.Asser;

/**
 * @author ZZZank
 */
public class TSKeyofType extends BaseType {
    public BaseType inner;

    public TSKeyofType(BaseType inner) {
        this.inner = Asser.tNotNull(inner, "inner");
    }

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return inner.getImportInfos(type);
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        return String.format("(keyof %s)", inner.line(declaration));
    }
}

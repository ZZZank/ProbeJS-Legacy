package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.Collections;
import java.util.List;

/**
 * Represents a type declaration. Standalone members are always exported.
 */
public class TypeDecl extends CommentableCode {
    public boolean exportDecl = true;

    public final String symbol;
    @NotNull
    public List<BaseType> symbolVariables = Collections.emptyList();

    @NotNull
    public BaseType type;
    @NotNull
    public BaseType.FormatType typeFormat = BaseType.FormatType.INPUT;

    public TypeDecl(String symbol, @NotNull BaseType type) {
        this.symbol = symbol;
        this.type = type;
    }

    public TypeDecl(String symbol, @NotNull List<BaseType> symbolVariables, @NotNull BaseType type) {
        this.symbol = symbol;
        this.symbolVariables = symbolVariables;
        this.type = type;
    }

    public TypeDecl setExport(boolean exportDecl) {
        this.exportDecl = exportDecl;
        return this;
    }

    @Override
    public ImportInfos getImportInfos() {
        return type.getImportInfos(BaseType.FormatType.INPUT);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        val builder = new StringBuilder();
        if (exportDecl) {
            builder.append("export ");
        }
        builder.append("type ").append(symbol);
        if (!symbolVariables.isEmpty()) {
            builder.append(Types.join(", ", "<", ">", symbolVariables)
                .line(declaration, BaseType.FormatType.VARIABLE));
        }
        builder.append(" = ")
            .append(type.line(declaration, typeFormat))
            .append(";");
        return Collections.singletonList(builder.toString());
    }
}

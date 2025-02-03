package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.Asser;

import java.util.Collections;
import java.util.List;

/**
 * Represents a type declaration. Standalone members are always exported.
 */
public class TypeDecl extends CommentableCode {
    public boolean exportDecl;

    public final String symbol;
    @NotNull
    public List<? extends BaseType> symbolVariables;

    @NotNull
    public BaseType type;
    @NotNull
    public BaseType.FormatType typeFormat;

    public TypeDecl(String symbol, @NotNull BaseType type) {
        this(symbol, Collections.emptyList(), type);
    }

    public TypeDecl(String symbol, @NotNull List<? extends BaseType> symbolVariables, @NotNull BaseType type) {
        this(true, symbol, symbolVariables, type, BaseType.FormatType.INPUT);
    }

    public TypeDecl(
        boolean exportDecl,
        String symbol,
        @NotNull List<? extends BaseType> symbolVariables,
        @NotNull BaseType type,
        @NotNull BaseType.FormatType typeFormat
    ) {
        this.exportDecl = exportDecl;
        this.symbol = symbol;
        this.symbolVariables = Asser.tNotNull(symbolVariables, "symbolVariables");
        this.type = Asser.tNotNull(type, "type");
        this.typeFormat = Asser.tNotNull(typeFormat, "typeFormat");
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

    public static class Builder {
        private final String symbol;
        private final BaseType type;
        private boolean exportDecl = true;
        private List<? extends BaseType> symbolVariables = Collections.emptyList();
        private BaseType.FormatType typeFormat = BaseType.FormatType.INPUT;

        public Builder(String symbol, @NotNull BaseType type) {
            this.symbol = symbol;
            this.type = type;
        }

        public Builder exportDecl(boolean exportDecl) {
            this.exportDecl = exportDecl;
            return this;
        }

        public Builder symbolVariables(@NotNull List<? extends BaseType> symbolVariables) {
            this.symbolVariables = symbolVariables;
            return this;
        }

        public Builder typeFormat(@NotNull BaseType.FormatType typeFormat) {
            this.typeFormat = typeFormat;
            return this;
        }

        public TypeDecl build() {
            return new TypeDecl(this.exportDecl, this.symbol, this.symbolVariables, this.type, this.typeFormat);
        }

        public String toString() {
            return "TypeDecl.TypeDeclBuilder(exportDecl=" + this.exportDecl + ", symbol=" + this.symbol
                + ", symbolVariables=" + this.symbolVariables + ", type=" + this.type + ", typeFormat="
                + this.typeFormat + ")";
        }
    }
}

package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.Asser;

import java.util.Collections;
import java.util.List;

/**
 * Represents a type declaration.
 */
public class TypeDecl extends CommentableCode {
    public boolean exportDecl;

    public String name;
    @NotNull
    public List<TSVariableType> symbolVariables;

    @NotNull
    public BaseType type;
    @NotNull
    public BaseType.FormatType typeFormat;

    public TypeDecl(String name, @NotNull BaseType type) {
        this(name, Collections.emptyList(), type);
    }

    public TypeDecl(String name, @NotNull List<TSVariableType> symbolVariables, @NotNull BaseType type) {
        this(true, name, symbolVariables, type, BaseType.FormatType.INPUT);
    }

    public TypeDecl(
        boolean exportDecl,
        String name,
        @NotNull List<TSVariableType> symbolVariables,
        @NotNull BaseType type,
        @NotNull BaseType.FormatType typeFormat
    ) {
        this.exportDecl = exportDecl;
        this.name = name;
        this.symbolVariables = Asser.tNotNull(symbolVariables, "symbolVariables");
        this.type = Asser.tNotNull(type, "type");
        this.typeFormat = Asser.tNotNull(typeFormat, "typeFormat");
    }

    public TypeDecl setTypeFormat(@NotNull BaseType.FormatType typeFormat) {
        this.typeFormat = typeFormat;
        return this;
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos
            .of(type.getImportInfos(typeFormat))
            .fromCodes(symbolVariables, BaseType.FormatType.VARIABLE);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        val builder = new StringBuilder();
        if (exportDecl) {
            builder.append("export ");
        }
        builder.append("type ").append(name);
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
        private final TypeDecl decl;

        public Builder(TypeDecl toBuild) {
            this.decl = toBuild;
        }

        public Builder exportDecl(boolean value) {
            decl.exportDecl = value;
            return this;
        }

        public Builder name(String name) {
            decl.name = name;
            return this;
        }

        public Builder symbolVariables(List<TSVariableType> variableTypes) {
            decl.symbolVariables = variableTypes;
            return this;
        }

        public Builder type(BaseType type) {
            decl.type = type;
            return this;
        }

        public Builder typeFormat(BaseType.FormatType format) {
            decl.typeFormat = format;
            return this;
        }

        public TypeDecl build() {
            return decl;
        }
    }
}

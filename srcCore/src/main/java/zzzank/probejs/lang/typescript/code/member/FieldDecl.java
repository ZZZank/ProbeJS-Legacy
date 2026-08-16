package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.ts.VariableDeclaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FieldDecl extends CommentableCode {
    public boolean isFinal = false;
    public boolean isStatic = false;
    public String name;
    public BaseType type;

    public FieldDecl(String name, BaseType type) {
        this.name = name;
        this.type = type;
    }

    public VariableDeclaration asVariableDecl() {
        return new VariableDeclaration(this.name, this.type);
    }

    @Override
    public ImportInfos getImportInfos() {
        return type.getImportInfos(BaseType.FormatType.RETURN);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // [static] [readonly] "name": ReturnType;
        var builder = new StringBuilder();

        if (isStatic) {
            builder.append("static ");
        }
        if (isFinal) {
            builder.append("readonly ");
        }
        builder.append(ProbeJS.GSON.toJson(name));
        builder.append(": ").append(type.line(declaration, BaseType.FormatType.RETURN));
        builder.append(";");

        return Collections.singletonList(builder.toString());
    }
}

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
        val head = new ArrayList<String>();
        if (isStatic) {
            head.add("static");
        }
        if (isFinal) {
            head.add("readonly");
        }
        head.add(ProbeJS.GSON.toJson(name));

        return Collections.singletonList(String.format("%s: %s",
            String.join(" ", head),
            type.line(declaration, BaseType.FormatType.RETURN)
        ));
    }
}

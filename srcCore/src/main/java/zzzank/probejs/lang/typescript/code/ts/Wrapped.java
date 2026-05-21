package zzzank.probejs.lang.typescript.code.ts;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.DeclarationCode;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.DocUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Wrapped extends CommentableCode implements DeclarationCode {
    public final List<Code> codes = new ArrayList<>();

    public void addCode(Code inner) {
        this.codes.add(inner);
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of().fromCodes(codes);
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    public void merge(Wrapped other) {
        this.codes.addAll(other.codes);
    }

    @Override
    public void reportDeclaredNames(Set<String> existed) {
        for (var code : codes) {
            if (code instanceof DeclarationCode declarationCode) {
                declarationCode.reportDeclaredNames(existed);
            }
        }
    }

    public static class Global extends Wrapped {
        @Override
        public List<String> formatRaw(Declaration declaration) {
            val lines = new ArrayList<String>();
            lines.add("declare global {");
            DocUtils.addIndentedCodes(lines, codes, declaration);
            lines.add("}");
            return lines;
        }
    }

    public static class Namespace extends Wrapped {
        public final String nameSpace;

        public Namespace(String nameSpace) {
            this.nameSpace = nameSpace;
        }

        @Override
        public List<String> formatRaw(Declaration declaration) {
            val lines = new ArrayList<String>();
            lines.add(String.format("export namespace %s {", nameSpace));
            DocUtils.addIndentedCodes(lines, codes, declaration);
            lines.add("}");
            return lines;
        }
    }
}

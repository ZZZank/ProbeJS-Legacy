package zzzank.probejs.lang.typescript.code.ts;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.ArrayList;
import java.util.List;

public abstract class Wrapped extends CommentableCode {
    public final List<Code> codes = new ArrayList<>();

    public void addCode(Code inner) {
        this.codes.add(inner);
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of().fromCodes(codes);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        val lines = new ArrayList<String>();
        for (val code : codes) {
            lines.addAll(code.format(declaration));
        }
        return lines;
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    public void merge(Wrapped other) {
        this.codes.addAll(other.codes);
    }

    public static class Global extends Wrapped {
        @Override
        public List<String> formatRaw(Declaration declaration) {
            val formatted = super.formatRaw(declaration);
            val lines = new ArrayList<String>(formatted.size() + 2);
            lines.add("declare global {");
            lines.addAll(formatted);
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
            val formatted = super.formatRaw(declaration);
            val lines = new ArrayList<String>(formatted.size() + 2);
            lines.add(String.format("export namespace %s {",nameSpace));
            lines.addAll(formatted);
            lines.add("}");
            return lines;
        }
    }
}

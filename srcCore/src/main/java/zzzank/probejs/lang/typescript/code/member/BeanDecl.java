package zzzank.probejs.lang.typescript.code.member;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank
 */
public abstract class BeanDecl extends CommentableCode {
    public String name;
    public BaseType type;

    public BeanDecl(String name, BaseType type) {
        this.name = name;
        this.type = type;
    }

    public static class Getter extends BeanDecl {
        public Getter(String name, BaseType type) {
            super(name, type);
        }

        @Override
        public List<String> formatRaw(Declaration declaration) {
            return Collections.singletonList(String.format(
                "get %s(): %s",
                ProbeJS.GSON.toJson(name),
                type.line(declaration, BaseType.FormatType.RETURN)
            ));
        }

        @Override
        public ImportInfos getImportInfos() {
            return type.getImportInfos(BaseType.FormatType.RETURN);
        }
    }

    public static class Setter extends BeanDecl {
        @NotNull
        public String paramName = "value";

        public Setter(String name, BaseType type) {
            super(name, type);
        }

        @Override
        public List<String> formatRaw(Declaration declaration) {
            return Collections.singletonList(String.format(
                "set %s(%s: %s)",
                ProbeJS.GSON.toJson(name),
                paramName,
                type.line(declaration, BaseType.FormatType.INPUT)
            ));
        }

        @Override
        public ImportInfos getImportInfos() {
            return type.getImportInfos(BaseType.FormatType.INPUT);
        }
    }
}

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

    @Override
    public ImportInfos getImportInfos() {
        return type.getImportInfos(getFormatType());
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        return Collections.singletonList(String.format(
            getFormat(),
            ProbeJS.GSON.toJson(name),
            type.line(declaration, getFormatType())
        ));
    }

    @NotNull
    protected abstract BaseType.FormatType getFormatType();

    @NotNull
    protected abstract String getFormat();

    public static class Getter extends BeanDecl {
        public Getter(String name, BaseType type) {
            super(name, type);
        }

        @Override
        protected @NotNull String getFormat() {
            return "get %s(): %s";
        }

        @Override
        protected BaseType.@NotNull FormatType getFormatType() {
            return BaseType.FormatType.RETURN;
        }
    }

    public static class Setter extends BeanDecl {
        public Setter(String name, BaseType type) {
            super(name, type);
        }

        @Override
        protected @NotNull String getFormat() {
            return "set %s(value: %s)";
        }

        @Override
        protected BaseType.@NotNull FormatType getFormatType() {
            return BaseType.FormatType.INPUT;
        }
    }
}

package zzzank.probejs.lang.typescript.code.type.js;

import lombok.Data;
import lombok.experimental.Accessors;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;

import java.util.function.Function;

@Accessors(fluent = true)
@Data
public class JSParam {
    private final String name;
    private final boolean optional;
    private final BaseType type;

    public String format(
        Declaration declaration,
        BaseType.FormatType formatType,
        Function<String, String> nameProcessor
    ) {
        return String.format(
            "%s%s: %s",
            nameProcessor.apply(name),
            optional ? "?" : "",
            type.line(declaration, formatType)
        );
    }

    public static class Literal extends JSParam {
        public Literal(String name, boolean optional, BaseType type) {
            super(name, optional, type);
        }

        @Override
        public String format(Declaration declaration, BaseType.FormatType type, Function<String, String> ignored) {
            return super.format(declaration, type, Function.identity());
        }
    }
}

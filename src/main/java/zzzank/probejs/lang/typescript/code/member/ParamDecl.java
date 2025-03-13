package zzzank.probejs.lang.typescript.code.member;

import lombok.ToString;
import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@ToString
public final class ParamDecl {
    public String name;
    public BaseType type;
    public boolean varArg;
    public boolean optional;

    public ParamDecl(String name, BaseType type, boolean varArg, boolean optional) {
        this.name = name;
        this.type = type;
        this.varArg = varArg;
        this.optional = optional;
    }

    public ParamDecl(String name, BaseType type) {
        this(name, type, false, false);
    }

    public String format(int i, Declaration declaration, BaseType.FormatType formatType) {
        val builder = new StringBuilder();
        if (varArg) {
            builder.append("...");
        }
        builder.append(getArgName(i));
        if (optional) {
            builder.append("?");
        }
        return builder.append(": ").append(type.line(declaration, formatType)).toString();
    }

    private String getArgName(int i) {
        if (!NameUtils.isTSIdentifier(name)) {
            return "arg" + i;
        }
        var out = name;
        while (!NameUtils.isNameSafe(out)) {
            out = out + "_";
        }
        return out;
    }

    public static String formatParams(List<ParamDecl> params, Declaration declaration) {
        return formatParams(params, declaration, BaseType.FormatType.INPUT);
    }

    public static String formatParams(List<ParamDecl> params, Declaration declaration, BaseType.FormatType formatType) {
        val formatted = new ArrayList<String>(params.size());
        for (int i = 0; i < params.size(); i++) {
            val param = params.get(i);
            formatted.add(param.format(i, declaration, formatType));
        }
        return formatted.stream().collect(Collectors.joining(", ", "(", ")"));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ParamDecl paramDecl
            && varArg == paramDecl.varArg
            && optional == paramDecl.optional
            && Objects.equals(name, paramDecl.name)
            && Objects.equals(type, paramDecl.type);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(name);
        result = 31 * result + Objects.hashCode(type);
        result = 31 * result + Boolean.hashCode(varArg);
        result = 31 * result + Boolean.hashCode(optional);
        return result;
    }
}

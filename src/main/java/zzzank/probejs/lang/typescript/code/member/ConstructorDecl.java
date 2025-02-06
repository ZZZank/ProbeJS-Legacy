package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;

public class ConstructorDecl extends CommentableCode {
    public final List<? extends BaseType> variableTypes;
    public final List<ParamDecl> params;
    public String content = null;

    public ConstructorDecl(List<? extends BaseType> variableTypes, List<ParamDecl> params) {
        this.variableTypes = variableTypes;
        this.params = params;
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of()
            .fromCodes(variableTypes, BaseType.FormatType.VARIABLE)
            .fromCodes(params.stream().map(p -> p.type), BaseType.FormatType.INPUT);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // Format head - constructor<T>
        String head = "constructor";

        if (!variableTypes.isEmpty()) {
            head += Types.join(", ", "<", ">", variableTypes)
                .line(declaration, BaseType.FormatType.VARIABLE);
        }

        // Format body - (a: type, ...)
        val body = ParamDecl.formatParams(params, declaration);

        // Format tail - {/** content */}
        String tail = "";
        if (content != null) {
            tail = String.format("%s {/** %s */}", tail, content);
        }
        return Collections.singletonList(String.format("%s%s%s", head, body, tail));
    }

    public static class Builder {
        public final List<BaseType> variableTypes = new ArrayList<>();
        public final List<ParamDecl> params = new ArrayList<>();

        public Builder typeVariables(String... symbols) {
            return typeVariables(CollectUtils.mapToList(symbols, Types::generic));
        }

        public Builder typeVariables(BaseType... variableTypes) {
            return typeVariables(Arrays.asList(variableTypes));
        }

        public Builder typeVariables(Collection<? extends BaseType> variableTypes) {
            this.variableTypes.addAll(variableTypes);
            return this;
        }

        public Builder param(String symbol, BaseType type) {
            return param(symbol, type, false);
        }

        public Builder param(String symbol, BaseType type, boolean isOptional) {
            return param(symbol, type, isOptional, false);
        }

        public Builder param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
            params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
            return this;
        }

        public final ConstructorDecl buildAsConstructor() {
            return new ConstructorDecl(variableTypes, params);
        }
    }
}

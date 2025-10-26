package zzzank.probejs.lang.typescript.code.member;

import lombok.val;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;

public class ConstructorDecl extends CommentableCode {
    public final List<TSVariableType> variableTypes;
    public final List<ParamDecl> params;
    public String content = null;

    public ConstructorDecl(List<TSVariableType> variableTypes, List<ParamDecl> params) {
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

    public static class BuilderBase<SELF extends BuilderBase<SELF>> {
        public final List<TSVariableType> variableTypes = new ArrayList<>();
        public final List<ParamDecl> params = new ArrayList<>();

        public SELF typeVariables(String... symbols) {
            return typeVariables(CollectUtils.mapToList(symbols, Types::generic));
        }

        public SELF typeVariables(TSVariableType... variableTypes) {
            return typeVariables(Arrays.asList(variableTypes));
        }

        public SELF typeVariables(Collection<TSVariableType> variableTypes) {
            this.variableTypes.addAll(variableTypes);
            return Cast.to(this);
        }

        public SELF param(String symbol, BaseType type) {
            return param(symbol, type, false);
        }

        public SELF param(String symbol, BaseType type, boolean isOptional) {
            return param(symbol, type, isOptional, false);
        }

        public SELF param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
            params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
            return Cast.to(this);
        }

        public final ConstructorDecl buildAsConstructor() {
            return new ConstructorDecl(variableTypes, params);
        }
    }

    public static class Builder extends BuilderBase<Builder> {
    }
}

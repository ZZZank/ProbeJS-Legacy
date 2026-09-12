package zzzank.probejs.lang.typescript.code.ts;

import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.DeclarationCode;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.*;

public class FunctionDeclaration extends CommentableCode implements DeclarationCode {
    public String name;
    public final List<TSVariableType> variableTypes;
    public final List<ParamDecl> params;
    public BaseType returnType;

    public FunctionDeclaration(String name, List<TSVariableType> variableTypes, List<ParamDecl> params, BaseType returnType) {
        this.name = name;
        this.variableTypes = variableTypes;
        this.params = params;
        this.returnType = returnType;
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of()
            .fromCode(returnType, BaseType.FormatType.RETURN)
            .fromCodes(variableTypes, BaseType.FormatType.VARIABLE)
            .fromCodes(params.stream().map(p -> p.type), BaseType.FormatType.INPUT);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // function name<T, T2>(param1: ParamType1): ReturnType;
        String result = "function " + name
            + TSVariableType.formatGenericParam(this.variableTypes, declaration)
            + ParamDecl.formatParams(params, declaration)
            + ": " + returnType.line(declaration, BaseType.FormatType.RETURN)
            + ";";

        return Collections.singletonList(result);
    }

    @Override
    public void reportDeclaredNames(Set<String> existed) {
        existed.add(name);
    }

    public static class Builder {
        public final String name;
        public final List<TSVariableType> variableTypes = new ArrayList<>();
        public final List<ParamDecl> params = new ArrayList<>();
        public BaseType returnType = Types.VOID;

        public Builder(String name) {
            this.name = name;
        }

        public Builder variable(String... symbols) {
            for (String symbol : symbols) {
                variable(Types.generic(symbol));
            }
            return this;
        }

        public Builder variable(TSVariableType... variableType) {
            variableTypes.addAll(Arrays.asList(variableType));
            return this;
        }

        public Builder returnType(BaseType type) {
            this.returnType = type;
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


        public FunctionDeclaration build() {
            return new FunctionDeclaration(
                name,
                variableTypes,
                params,
                returnType
            );
        }
    }
}

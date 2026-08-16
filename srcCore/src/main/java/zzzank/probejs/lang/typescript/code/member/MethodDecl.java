package zzzank.probejs.lang.typescript.code.member;

import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.CommentableCode;
import zzzank.probejs.lang.typescript.code.ts.FunctionDeclaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MethodDecl extends CommentableCode {
    public String name;
    public boolean isAbstract = false;
    public boolean isStatic = false;
    public List<TSVariableType> variableTypes;
    public List<ParamDecl> params;
    public BaseType returnType;
    public String content = null;

    public MethodDecl(String name, List<TSVariableType> variableTypes, List<ParamDecl> params, BaseType returnType) {
        this.name = name;
        this.variableTypes = new ArrayList<>(variableTypes);
        this.params = new ArrayList<>(params);
        this.returnType = returnType;
    }

    public FunctionDeclaration asFunctionDecl() {
        return new FunctionDeclaration(
            this.name,
            this.variableTypes,
            this.params,
            this.returnType
        );
    }

    @Override
    public ImportInfos getImportInfos() {
        return ImportInfos.of(returnType.getImportInfos(BaseType.FormatType.RETURN))
            .fromCodes(variableTypes, BaseType.FormatType.VARIABLE)
            .fromCodes(params.stream().map(p -> p.type), BaseType.FormatType.INPUT);
    }

    @Override
    public List<String> formatRaw(Declaration declaration) {
        // [static] "name"<T, T2 extends string>(param1: ParamType1): ReturnType [{/** content */}]
        var result = new StringBuilder();

        if (isStatic) {
            result.append("static ");
        }
        result.append(ProbeJS.GSON.toJson(name));
        result.append(TSVariableType.formatGenericParam(variableTypes, declaration));
        result.append(ParamDecl.formatParams(params, declaration));
        result.append(": ").append(returnType.line(declaration, BaseType.FormatType.RETURN));
        if (content != null) {
            result.append(" {/** ").append(content).append(" */}");
        }

        return Collections.singletonList(result.toString());
    }

    public static class Builder extends ConstructorDecl.BuilderBase<Builder> {
        public final String name;
        public BaseType returnType = Types.VOID;
        public boolean isAbstract = false;
        public boolean isStatic = false;

        public Builder(String name) {
            this.name = name;
        }

        public Builder returnType(BaseType type) {
            this.returnType = type;
            return this;
        }

        public Builder abstractMethod() {
            this.isAbstract = true;
            return this;
        }

        public Builder staticMethod() {
            this.isStatic = true;
            return this;
        }

        public MethodDecl buildAsMethod() {
            var decl = new MethodDecl(name, variableTypes, params, returnType);
            decl.isAbstract = isAbstract;
            decl.isStatic = isStatic;
            return decl;
        }
    }
}

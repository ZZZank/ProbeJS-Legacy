package zzzank.probejs.lang.typescript.code.type.js;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.utils.Cast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * {@code (a: A$$Type, b: B$$Type) => C} if format type is {@link zzzank.probejs.lang.typescript.code.type.BaseType.FormatType#RETURN}
 * <p>
 * {@code (a: A, b: B) => C$$Type} if format type is {@link zzzank.probejs.lang.typescript.code.type.BaseType.FormatType#INPUT}
 * @author ZZZank
 */
@RequiredArgsConstructor
public class JSLambdaType extends BaseType {
    public final List<ParamDecl> params;
    public final BaseType returnType;
    @NotNull
    public List<TSVariableType> variableTypes = List.of();

    @Override
    public ImportInfos getImportInfos(@NotNull FormatType type) {
        return ImportInfos.of(returnType.getImportInfos(type))
            .fromCodes(variableTypes)
            .fromCodes(params.stream().map(p -> p.type), paramFormatType(type));
    }

    public static FormatType paramFormatType(FormatType formatType) {
        return switch (formatType) {
            case RETURN -> FormatType.INPUT;
            case INPUT -> FormatType.RETURN;
            default -> formatType;
        };
    }

    @Override
    public String line(Declaration declaration, FormatType formatType) {
        // (arg0: type, arg1: type...) => returnType
        return String.format(
            "(%s%s => %s)",
            TSVariableType.formatGenericParam(this.variableTypes, declaration),
            //when formatType is INPUT, aka this lambda is a param itself, params of this lambda should be concrete
            ParamDecl.formatParams(
                params,
                declaration,
                paramFormatType(formatType)
            ),
            returnType.line(declaration, formatType)
        );
    }

    public MethodDecl asMethod(String methodName) {
        return new MethodDecl(methodName, Collections.emptyList(), params, returnType);
    }

    public static class BuilderBase<SELF extends BuilderBase<SELF>> {
        public final List<ParamDecl> params = new ArrayList<>();
        public BaseType returnType = Types.VOID;

        protected SELF self() {
            return Cast.to(this);
        }

        public SELF returnType(BaseType type) {
            this.returnType = type;
            return self();
        }

        public SELF param(String symbol, BaseType type) {
            return param(symbol, type, false);
        }

        public SELF param(String symbol, BaseType type, boolean isOptional) {
            return param(symbol, type, isOptional, false);
        }

        public SELF param(String symbol, BaseType type, boolean isOptional, boolean isVarArg) {
            params.add(new ParamDecl(symbol, type, isVarArg, isOptional));
            return self();
        }

        public JSLambdaType build() {
            return new JSLambdaType(params, returnType);
        }
    }

    public static class Builder extends BuilderBase<Builder> {
        public List<TSVariableType> variableTypes = List.of();

        public Builder variableTypes(Collection<? extends TSVariableType> variableTypes) {
            this.variableTypes = List.copyOf(variableTypes);
            return this;
        }
    }
}

package zzzank.probejs.lang.typescript.code.type.utility;

import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.utils.NameUtils;

import java.util.Locale;

/**
 * @author ZZZank
 */
public interface TSUtilityType {
    static TSParamType awaited(BaseType promise) {
        return Base.AWAITED.of(promise);
    }

    /**
     * Constructs a type with all properties of `Type` set to optional.
     * <p>
     * This utility will return a type that represents all subsets of a given type.
     */
    static TSParamType partial(BaseType type) {
        return Base.PARTIAL.of(type);
    }

    static TSParamType required(BaseType type) {
        return Base.REQUIRED.of(type);
    }

    static TSParamType readonly(BaseType promise) {
        return Base.READONLY.of(promise);
    }

    static TSParamType record(BaseType keys, BaseType type) {
        return Base.RECORD.of(keys, type);
    }

    static TSParamType pick(BaseType type, BaseType keys) {
        return Base.PICK.of(type, keys);
    }

    static TSParamType omit(BaseType type, BaseType keys) {
        return Base.OMIT.of(type, keys);
    }

    static TSParamType exclude(BaseType type, BaseType excludeFilter) {
        return Base.EXCLUDE.of(type, excludeFilter);
    }

    static TSParamType extract(BaseType type, BaseType extractFilter) {
        return Base.EXTRACT.of(type, extractFilter);
    }

    static TSParamType nonNullable(BaseType type) {
        return Base.NON_NULLABLE.of(type);
    }

    static TSParamType parameters(BaseType function) {
        return Base.PARAMETERS.of(function);
    }

    static TSParamType constructorParameters(BaseType type) {
        return Base.CONSTRUCTOR_PARAMETERS.of(type);
    }

    static TSParamType returnType(BaseType type) {
        return Base.RETURN_TYPE.of(type);
    }

    static TSParamType instanceType(BaseType type) {
        return Base.INSTANCE_TYPE.of(type);
    }

    static TSParamType noInfer(BaseType type) {
        return Base.NO_INFER.of(type);
    }

    static TSParamType thisParameterType(BaseType type) {
        return Base.THIS_PARAMETER_TYPE.of(type);
    }

    static TSParamType omitThisParameter(BaseType type) {
        return Base.OMIT_THIS_PARAMETER.of(type);
    }

    static TSParamType thisType(BaseType type) {
        return Base.THIS_TYPE.of(type);
    }

    static TSParamType upperCase(BaseType string) {
        return Base.UPPERCASE.of(string);
    }

    static TSParamType lowerCase(BaseType string) {
        return Base.LOWERCASE.of(string);
    }

    static TSParamType capitalize(BaseType string) {
        return Base.CAPITALIZE.of(string);
    }

    static TSParamType unCapitalize(BaseType string) {
        return Base.UNCAPITALIZE.of(string);
    }


    enum Base {
        AWAITED,
        PARTIAL,
        REQUIRED,
        READONLY,
        RECORD,
        PICK,
        OMIT,
        EXCLUDE,
        EXTRACT,
        NON_NULLABLE,
        PARAMETERS,
        CONSTRUCTOR_PARAMETERS,
        RETURN_TYPE,
        INSTANCE_TYPE,
        NO_INFER,
        THIS_PARAMETER_TYPE,
        OMIT_THIS_PARAMETER,
        THIS_TYPE,
        UPPERCASE,
        LOWERCASE,
        CAPITALIZE,
        UNCAPITALIZE,
        ;
        public final JSPrimitiveType type = Types.primitive(
            NameUtils.snakeToTitle(this.name().toLowerCase(Locale.ROOT))
        );

        public TSParamType of(BaseType... params) {
            return Types.parameterized(type, params);
        }
    }

    /*
    Awaited<Type>
    Partial<Type>
    Required<Type>
    Readonly<Type>
    Record<Keys, Type>
    Pick<Type, Keys>
    Omit<Type, Keys>
    Exclude<UnionType, ExcludedMembers>
    Extract<Type, Union>
    NonNullable<Type>
    Parameters<Type>
    ConstructorParameters<Type>
    ReturnType<Type>
    InstanceType<Type>
    NoInfer<Type>
    ThisParameterType<Type>
    OmitThisParameter<Type>
    ThisType<Type>

    Intrinsic String Manipulation Types
        Uppercase<StringType>
        Lowercase<StringType>
        Capitalize<StringType>
        Uncapitalize<StringType>
     */
}

package zzzank.probejs.lang.typescript.code.type;

import dev.latvian.mods.rhino.util.HideFromJS;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.code.type.js.*;
import zzzank.probejs.lang.typescript.code.type.ts.*;
import zzzank.probejs.lang.typescript.code.type.utility.*;
import zzzank.probejs.lang.typescript.refer.ImportInfos;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Types {
    JSPrimitiveType ANY = primitive("any");
    JSPrimitiveType BOOLEAN = primitive("boolean");
    JSPrimitiveType NUMBER = primitive("number");
    JSPrimitiveType STRING = primitive("string");
    JSPrimitiveType NEVER = primitive("never");
    JSPrimitiveType UNKNOWN = primitive("unknown");
    JSPrimitiveType VOID = primitive("void");
    JSPrimitiveType THIS = primitive("this");
    JSPrimitiveType OBJECT = primitive("object");
    JSPrimitiveType NULL = primitive("null");
    JSPrimitiveType UNDEFINED = primitive("undefined");
    JSTupleType EMPTY_ARRAY = Types.tuple().build();
    JSObjectType EMPTY_OBJECT = Types.object().build();

    /**
     * Returns a literal type of the input if it's something OK in TS,
     * otherwise, any will be returned.
     *
     * @deprecated selecting this method overload means that your 'content' actually cannot be converted to JS literal
     * , and only 'any' will be returned
     * @param content a string, number or boolean
     */
    static JSPrimitiveType literal(Object content) {
        return content instanceof String
            || content instanceof Number
            || content instanceof Boolean
            || content instanceof Character
            ? primitive(ProbeJS.GSON.toJson(content))
            : ANY;
    }

    @HideFromJS
    static JSPrimitiveType literal(String content) {
        return primitive(ProbeJS.GSON.toJson(content));
    }

    @HideFromJS
    static JSPrimitiveType literal(Number content) {
        return primitive(ProbeJS.GSON.toJson(content));
    }

    @HideFromJS
    static JSPrimitiveType literal(Boolean content) {
        return primitive(ProbeJS.GSON.toJson(content));
    }

    @HideFromJS
    static JSPrimitiveType literal(Character content) {
        return primitive(ProbeJS.GSON.toJson(content));
    }

    /**
     * Returns a type that will be as-is in the TypeScript to represent
     * keywords/types not covered, e.g. InstanceType.
     */
    static JSPrimitiveType primitive(String type) {
        return new JSPrimitiveType(type);
    }

    static JSTupleType.Builder tuple() {
        return new JSTupleType.Builder();
    }

    static TSArrayType array(BaseType base) {
        return new TSArrayType(base);
    }

    static BaseType and(BaseType... types) {
        return and(Arrays.asList(types));
    }

    static BaseType and(Collection<? extends BaseType> types) {
        return types.isEmpty() ? NEVER : new JSJoinedType.Intersection(types);
    }

    static BaseType or(BaseType... types) {
        return or(Arrays.asList(types));
    }

    static BaseType or(Collection<? extends BaseType> types) {
        return types.isEmpty() ? NEVER : new JSJoinedType.Union(types);
    }

    static JSJoinedType join(CharSequence delimiter, BaseType... types) {
        return join(delimiter, Arrays.asList(types));
    }

    static JSJoinedType join(CharSequence delimiter, Collection<? extends BaseType> types) {
        return join(delimiter, "", "", types);
    }

    static JSJoinedType join(CharSequence delimiter, CharSequence prefix, CharSequence suffix, BaseType... types) {
        return join(delimiter, prefix, suffix, Arrays.asList(types));
    }

    static JSJoinedType join(
        @NotNull CharSequence delimiter,
        @NotNull CharSequence prefix,
        @NotNull CharSequence suffix,
        @NotNull Collection<? extends BaseType> types
    ) {
        return new JSJoinedType.Custom(Objects.requireNonNull(types), delimiter, prefix, suffix);
    }

    static TSParamType parameterized(BaseType base, BaseType... params) {
        return parameterized(base, Arrays.asList(params));
    }

    static TSParamType parameterized(BaseType base, Collection<? extends BaseType> params) {
        return new TSParamType(base, params);
    }

    static TSVariableType generic(String symbol) {
        return generic(symbol, null);
    }

    static TSVariableType generic(String symbol, BaseType extendOn) {
        return generic(symbol, extendOn, null);
    }

    static TSVariableType generic(String symbol, BaseType extendOn, BaseType defaultTo) {
        return new TSVariableType(symbol, extendOn, defaultTo);
    }

    static BaseType typeMaybeGeneric(Class<?> clazz) {
        var typeParameters = clazz.getTypeParameters();
        if (typeParameters.length == 0) {
            return type(clazz);
        }

        return parameterized(
            type(clazz),
            Collections.nCopies(typeParameters.length, ANY).toArray(new BaseType[0])
        );
    }

    /**
     * You should ensure that this Class does not have type parameters.
     * <br>
     * Otherwise, use typeMaybeGeneric
     */
    static TSClassType type(Class<?> clazz) {
        return type(ClassPath.fromJava(clazz));
    }

    static TSClassType type(ClassPath classPath) {
        return new TSClassType(classPath);
    }

    static JSTypeOfType typeOf(Class<?> clazz) {
        return typeOf(ClassPath.fromJava(clazz));
    }

    static JSTypeOfType typeOf(ClassPath classPath) {
        return typeOf(type(classPath));
    }

    static JSTypeOfType typeOf(BaseType classType) {
        if (classType instanceof TSClassType cType
            && Optional.ofNullable(cType.classPath.toClazz(ClassRegistry.REGISTRY))
            .map(Clazz::isInterface)
            .orElse(false)
        ) {
            classType = staticType(cType.classPath);
        }
        return new JSTypeOfType(classType);
    }

    static <T extends BaseType> ContextShield<T> contextShield(T type, BaseType.FormatType formatType) {
        return new ContextShield<>(type, formatType);
    }

    static CustomType custom(
        BiFunction<Declaration, BaseType.FormatType, String> formatter,
        Function<BaseType.FormatType, ImportInfos> imports
    ) {
        return new CustomType(formatter, imports);
    }

    static CustomType custom(
        BiFunction<Declaration, BaseType.FormatType, String> formatter
    ) {
        return custom(formatter, (t) -> ImportInfos.of());
    }

    static <T extends BaseType> ImportShield<T> importShield(T type, ImportInfos imports) {
        return new ImportShield<>(type, imports);
    }

    static StaticType staticType(ClassPath path) {
        return new StaticType(path);
    }

    static JSLambdaType.Builder lambda() {
        return new JSLambdaType.Builder();
    }

    static JSObjectType.Builder object() {
        return new JSObjectType.Builder();
    }

    static TSOptionalType optional(BaseType type) {
        return new TSOptionalType(type);
    }

    static BaseType filter(BaseType type, Predicate<BaseType> typePredicate) {
        if (type instanceof JSJoinedType.Union union) {
            return Types.or(
                union.types.stream()
                    .filter((t) -> !typePredicate.test(t))
                    .map((t) -> filter(t, typePredicate))
                    .collect(Collectors.toList())
            );
        } else if (type instanceof JSJoinedType.Intersection intersection) {
            return Types.and(
                intersection.types.stream()
                    .filter((t) -> !typePredicate.test(t))
                    .map((t) -> filter(t, typePredicate))
                    .collect(Collectors.toList())
            );
        }
        return type;
    }

    static WithFormatType format(String format, BaseType... types) {
        return new WithFormatType(format, types);
    }

    static WithFormatType withComment(BaseType type, String comment) {
        return format("%s /* %s */", type, Types.primitive(comment));
    }

    static WithFormatType ternary(TSVariableType condition, BaseType ifTrue, BaseType ifFalse) {
        if (condition.extend == null) {
            throw new IllegalArgumentException("no 'extends' in provided variable type");
        }
        return format("%s ? %s : %s", condition, ifTrue, ifFalse);
    }
}

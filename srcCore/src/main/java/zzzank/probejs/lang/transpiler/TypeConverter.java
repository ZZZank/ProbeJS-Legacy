package zzzank.probejs.lang.transpiler;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.*;
import zzzank.probejs.lang.transpiler.redirect.TypeRedirect;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.utils.CollectUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Adapts a TypeDescriptor into a BaseType
 */
public class TypeConverter {

    public final List<TypeRedirect> typeRedirects = new ArrayList<>();

    public void addTypeRedirect(TypeRedirect redirect) {
        typeRedirects.add(Objects.requireNonNull(redirect));
    }

    public BaseType convertType(TypeDescriptor descriptor) {
        for (val typeRedirect : typeRedirects) {
            if (typeRedirect.test(descriptor, this)) {
                return typeRedirect.apply(descriptor, this);
            }
        }
        return convertTypeBuiltin(descriptor);
    }

    public BaseType convertTypeExcluding(TypeDescriptor descriptor, TypeRedirect excludedRedirect) {
        Objects.requireNonNull(excludedRedirect);
        for (val redirect : typeRedirects) {
            if (!excludedRedirect.equals(redirect) && redirect.test(descriptor, this)) {
                return redirect.apply(descriptor, this);
            }
        }
        return convertTypeBuiltin(descriptor);
    }

    public @NotNull BaseType convertTypeBuiltin(TypeDescriptor descriptor) {
        if (descriptor instanceof ClassType classType) {
            return Types.type(classType.classPath);
        } else if (descriptor instanceof ArrayType arrayType) {
            return convertType(arrayType.component).asArray();
        } else if (descriptor instanceof ParamType paramType) {
            val base = convertType(paramType.base);
            if (base == Types.ANY) {
                return Types.ANY;
            }
            val params = CollectUtils.mapToList(paramType.params, this::convertType);
            return Types.parameterized(base, params);
        } else if (descriptor instanceof VariableType variableType) {
            return convertVariableType(variableType);
        } else if (descriptor instanceof WildType wildType) {
            return wildType.stream().findAny().map(this::convertType).orElse(Types.ANY);
        }
        throw new RuntimeException("Unknown subclass of TypeDescriptor.");
    }

    public @NotNull TSVariableType convertVariableType(VariableType variableType) {
        val desc = variableType.getDescriptors();
        return switch (desc.size()) {
            case 0 -> Types.generic(variableType.getSymbol());
            case 1 -> Types.generic(variableType.getSymbol(), convertType(desc.get(0)));
            default -> Types.generic(
                variableType.getSymbol(),
                Types.and(CollectUtils.mapToList(desc, this::convertType))
            );
        };
    }

    public @NotNull TSVariableType convertVariableTypeWithDefault(VariableType variableType) {
        val desc = variableType.getDescriptors();
        return switch (desc.size()) {
            case 0 -> Types.genericAndDefault(variableType.getSymbol(), Types.ANY);
            case 1 -> Types.genericAndDefault(variableType.getSymbol(), convertType(desc.get(0)));
            default -> Types.genericAndDefault(
                variableType.getSymbol(),
                Types.and(CollectUtils.mapToList(desc, this::convertType))
            );
        };
    }

    public BaseType convertType(Type javaType) {
        return convertType(TypeAdapter.getTypeDescription(javaType));
    }
}

package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.ArrayType;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.utils.NameUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @see #autoParamName(TypeDescriptor, int)
 * @author ZZZank
 */
public class AutoParamRename implements ClassTransformer {

    public static final Pattern MATCH_ARG_N = Pattern.compile("^arg(\\d+)$");

    private final Predicate<String> matching;

    public AutoParamRename(Pattern matching) {
        this.matching = matching.asPredicate();
    }

    public AutoParamRename(Predicate<String> matching) {
        this.matching = Objects.requireNonNull(matching);
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        val size = Math.min(methodInfo.params.size(), methodDecl.params.size());
        for (int i = 0; i < size; i++) {
            val typeDesc = methodInfo.params.get(i).type;
            val param = methodDecl.params.get(i);
            autoRename(param, typeDesc, i);
        }
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        val size = Math.min(constructorInfo.params.size(), constructorDecl.params.size());
        for (int i = 0; i < size; i++) {
            val typeDesc = constructorInfo.params.get(i).type;
            val param = constructorDecl.params.get(i);
            autoRename(param, typeDesc, i);
        }
    }

    private void autoRename(ParamDecl param, TypeDescriptor typeDesc, int index) {
        if (this.matching.test(param.name)) {
            val autoName = autoParamName(typeDesc, index);
            if (autoName != null) {
                param.name = autoName;
            }
        }
    }

    @Nullable
    public static String autoParamName(TypeDescriptor type, int index) {
        if (type instanceof ClassType c) {
            val simpleName = c.clazz.getSimpleName();
            if (simpleName.isEmpty()) {
                return null;
            }
            return NameUtils.firstLower(simpleName) + index;
        } else if (type instanceof ArrayType arr) {
            return autoParamName(arr.component, index) + 's';
        } else if (type instanceof ParamType param) {
            return autoParamName(param.base, index);
        } else if (type instanceof VariableType vari) {
            return vari.getSymbol().toLowerCase(Locale.ROOT) + index;
        }
        return null;
    }
}

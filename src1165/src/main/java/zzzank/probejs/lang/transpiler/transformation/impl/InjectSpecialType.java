package zzzank.probejs.lang.transpiler.transformation.impl;

import lombok.val;
import net.minecraft.resources.ResourceKey;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.clazz.members.ParamInfo;
import zzzank.probejs.lang.java.type.impl.ClassType;
import zzzank.probejs.lang.java.type.impl.ParamType;
import zzzank.probejs.lang.transpiler.transformation.ClassTransformer;
import zzzank.probejs.lang.typescript.code.member.ConstructorDecl;
import zzzank.probejs.lang.typescript.code.member.MethodDecl;
import zzzank.probejs.lang.typescript.code.member.ParamDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.utils.CollectUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class InjectSpecialType implements ClassTransformer {
    public static final Set<ClassPath> NO_WRAPPING = new HashSet<>();

    static {
        NO_WRAPPING.add(ClassPath.fromJava(ResourceKey.class));
    }

    public static void modifyWrapping(ParamDecl param) {
        if (param.type instanceof TSParamType paramType
            && paramType.baseType instanceof TSClassType baseClass
            && NO_WRAPPING.contains(baseClass.classPath)
        ) {
            param.type = paramType.baseType.withParams(CollectUtils.mapToList(
                paramType.params,
                c -> c.contextShield(BaseType.FormatType.RETURN)
            ));
        }
    }

    private static int findReturnTypeIndex(Class<?> clazz) {
        val functional = Arrays
            .stream(clazz.getMethods())
            .filter(m -> Modifier.isAbstract(m.getModifiers()))
            .findFirst()
            .orElse(null);
        if (functional == null) {
            return -1;
        }
        if (functional.getGenericReturnType() instanceof TypeVariable<?> typeVariable) {
            TypeVariable<? extends Class<?>>[] typeVars = clazz.getTypeParameters();
            for (int i = 0; i < typeVars.length; i++) {
                if (typeVars[i].getName().equals(typeVariable.getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static void modifyLambda(ParamDecl param, ParamInfo info) {
        if (info.type instanceof ParamType paramType &&
            paramType.base instanceof ClassType classType &&
            classType.clazz.isAnnotationPresent(FunctionalInterface.class) &&
            param.type instanceof TSParamType tsParamType
        ) {
            val returnIndex = findReturnTypeIndex(classType.clazz);
            if (returnIndex < 0) {
                return;
            }
            val params = new ArrayList<>(tsParamType.params);
            params.set(returnIndex, params.get(returnIndex).contextShield(BaseType.FormatType.RETURN));
            param.type = Types.parameterized(tsParamType.baseType, params);
//            for (int i = 0; i < params.size(); i++) {
//                val p = params.get(i);
//                params.set(i, Types.contextShield(p, returnIndex == i ?
//                    BaseType.FormatType.INPUT :
//                    BaseType.FormatType.RETURN));
//            }
        }
    }

    @Override
    public void transformConstructor(Clazz clazz, ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        for (int i = 0; i < constructorDecl.params.size(); i++) {
            var param = constructorDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, constructorInfo.params.get(i));
        }
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        for (int i = 0; i < methodDecl.params.size(); i++) {
            var param = methodDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, methodInfo.params.get(i));
        }
    }
}

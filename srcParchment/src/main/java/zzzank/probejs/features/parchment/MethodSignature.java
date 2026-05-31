package zzzank.probejs.features.parchment;

import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.clazz.members.ParamInfo;

class MethodSignature {

    /// `forMethod(Lzzzank/probejs/lang/java/clazz/members/MethodInfo;)Ljava/lang/String;`
    public static String forMethod(MethodInfo method) {
        StringBuilder sb = new StringBuilder().append(method.name).append('(');
        for (ParamInfo param : method.params) {
            sb.append(toDescriptor(param.type.asClass()));
        }
        sb.append(')');
        sb.append(toDescriptor(method.returnType.asClass()));
        return sb.toString();
    }

    /// `<init>(Lzzzank/probejs/lang/java/clazz/members/ConstructorInfo;)V`
    public static String forConstructor(ConstructorInfo constructor) {
        StringBuilder sb = new StringBuilder("<init>(");
        for (ParamInfo param : constructor.params) {
            sb.append(toDescriptor(param.type.asClass()));
        }
        sb.append(")V");
        return sb.toString();
    }

    public static String toDescriptor(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == void.class) return "V";
            if (clazz == boolean.class) return "Z";
            if (clazz == byte.class) return "B";
            if (clazz == char.class) return "C";
            if (clazz == double.class) return "D";
            if (clazz == float.class) return "F";
            if (clazz == int.class) return "I";
            if (clazz == long.class) return "J";
            if (clazz == short.class) return "S";
        }
        if (clazz.isArray()) {
            return clazz.getName();
        }
        return "L" + clazz.getName().replace('.', '/') + ";";
    }
}

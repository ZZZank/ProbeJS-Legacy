package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.util.HideFromJS;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.lang.java.base.TypeVariableHolder;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.utils.CollectUtils;

import java.lang.reflect.Modifier;
import java.util.List;

public class Clazz extends TypeVariableHolder {

    public final ClassPath classPath;

    public final List<ConstructorInfo> constructors;
    public final List<FieldInfo> fields;
    public final List<MethodInfo> methods;

    @Nullable
    public final TypeDescriptor superClass;
    public final List<TypeDescriptor> interfaces;
    public final ClassAttribute attribute;

    public Clazz(Class<?> clazz, ClassPath path, MemberCollector collector) {
        super(clazz.getTypeParameters(), clazz.getAnnotations());

        this.classPath = path;

        collector.accept(clazz);
        this.constructors = collector.constructors().toList();
        this.methods = collector.methods().toList();
        this.fields = collector.fields().toList();

        this.superClass = clazz.getSuperclass() == Object.class
            ? null
            : TypeAdapter.getTypeDescription(clazz.getAnnotatedSuperclass());

        this.interfaces = CollectUtils.mapToList(clazz.getAnnotatedInterfaces(), TypeAdapter::getTypeDescription);
        this.attribute = new ClassAttribute(clazz);
    }

    public boolean isInterface() {
        return attribute.type == ClassType.INTERFACE;
    }

    @Override
    public int hashCode() {
        return classPath.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Clazz clazz && classPath.equals(clazz.classPath);
    }

    public Class<?> getOriginal() {
        return attribute.raw;
    }

    @Override
    public String toString() {
        return String.format("Clazz(%s)", this.attribute.raw);
    }

    public enum ClassType {
        INTERFACE,
        ENUM,
        RECORD,
        CLASS
    }

    public static class ClassAttribute {

        public final ClassType type;
        public final int modifiers;
        @HideFromJS
        public final Class<?> raw;

        public ClassAttribute(Class<?> clazz) {
            if (clazz.isInterface()) {
                this.type = ClassType.INTERFACE;
            } else if (clazz.isEnum()) {
                this.type = ClassType.ENUM;
//            } else if (clazz.isRecord()) {
//                this.type = ClassType.RECORD;
            } else {
                this.type = ClassType.CLASS;
            }

            modifiers = clazz.getModifiers();
            this.raw = clazz;
        }

        public boolean isAbstract() {
            return Modifier.isAbstract(modifiers);
        }
    }
}

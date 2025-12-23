package zzzank.probejs.lang.java.clazz.members;

import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.remap.RemapperBridge;
import zzzank.probejs.lang.java.type.TypeAdapter;
import zzzank.probejs.lang.java.type.TypeDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldInfo extends AnnotationHolder {
    public final String name;
    public final TypeDescriptor type;
    public final FieldAttributes attributes;

    public FieldInfo(Field field, String name) {
        super(field.getAnnotations());
        this.name = name;
        this.type = TypeAdapter.getTypeDescription(field.getAnnotatedType());
        this.attributes = new FieldAttributes(field);
    }

    @Override
    public String toString() {
        return String.format("FieldInfo(%s: %s)", name, type);
    }

    public static class FieldAttributes {
        public final boolean isFinal;
        public final boolean isStatic;
        private final Field field;

        public FieldAttributes(Field field) {
            int modifiers = field.getModifiers();
            this.isFinal = Modifier.isFinal(modifiers);
            this.isStatic = Modifier.isStatic(modifiers);
            this.field = field;
        }

        public Object getStaticValue() throws IllegalAccessException {
            if (!isStatic) {
                throw new RuntimeException("The field is not static!");
            }
            return field.get(null);
        }
    }
}

package zzzank.probejs.lang.java.type;

import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.base.ClassProvider;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.type.impl.VariableType;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TypeDescriptor extends AnnotationHolder implements ClassProvider {
    public TypeDescriptor(Annotation[] annotations) {
        super(annotations);
    }

    /**
     * Iterate through contained types.
     * <br>
     * For simple classes, the class yields itself.
     */
    public abstract Stream<TypeDescriptor> stream();

    /**
     * Return the class representation of this type
     */
    public Class<?> asClass() {
        return Object.class;
    }

    public abstract boolean canConsolidate();

    public abstract TypeDescriptor consolidate(Map<VariableType, TypeDescriptor> mapping);

    /**
     * Gets the class paths required to use the type.
     */
    public Collection<ClassPath> getClassPaths() {
        return stream()
            .map(TypeDescriptor::getClassPaths)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    /**
     * Gets the classes involved in the type.
     */
    public Collection<Class<?>> getClasses() {
        return stream()
            .map(TypeDescriptor::getClasses)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    }

    public static abstract class MaybeConsolidatable extends TypeDescriptor {
        // -1=unknown, 0=false, 1=true
        private byte consolidatable = -1;

        public MaybeConsolidatable(Annotation[] annotations) {
            super(annotations);
        }

        protected abstract TypeDescriptor consolidateImpl(Map<VariableType, TypeDescriptor> mapping);

        @Override
        public final TypeDescriptor consolidate(Map<VariableType, TypeDescriptor> mapping) {
            if (consolidatable < 0) {
                consolidatable = (byte) (canConsolidate() ? 1 : 0);
            }
            return consolidatable != 0 ? consolidateImpl(mapping) : this;
        }
    }
}

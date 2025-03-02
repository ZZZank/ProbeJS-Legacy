package zzzank.probejs.lang.java.type;

import zzzank.probejs.lang.java.base.AnnotationHolder;
import zzzank.probejs.lang.java.base.ClassProvider;
import zzzank.probejs.lang.java.clazz.ClassPath;

import java.lang.annotation.Annotation;
import java.util.Collection;
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

    /**
     * Gets the class paths required to use the type.
     */
    public Collection<ClassPath> getClassPaths() {
        return stream().flatMap(t -> t.getClassPaths().stream()).collect(Collectors.toSet());
    }

    /**
     * Gets the classes involved in the type.
     */
    public Collection<Class<?>> getClasses() {
        return stream().flatMap(t -> t.getClasses().stream()).collect(Collectors.toSet());
    }
}

package zzzank.probejs.lang.java.base;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.utils.Asser;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AnnotationHolder {

    public static final Annotation[] NO_ANNOTATION = new Annotation[0];

    @NotNull
    public final Annotation[] annotations;

    public AnnotationHolder(@NotNull Annotation @NotNull [] annotations) {
        this.annotations = annotations.length == 0
            ? NO_ANNOTATION // skip element nullability testing if empty
            : Asser.tNotNullAll(annotations, "annotations");
    }

    public boolean hasAnnotation(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> List<T> getAnnotations(Class<T> type) {
        return (List<T>) Arrays.stream(annotations)
            .filter(type::isInstance)
            .toList();
    }

    @Nullable
    public <T extends Annotation> T getAnnotation(Class<T> type) {
        for (val annotation : annotations) {
            if (type.isInstance(annotation)) {
                return (T) annotation;
            }
        }
        return null;
    }

    public <T extends Annotation> Optional<T> getAnnotationOptional(Class<T> type) {
        return Optional.ofNullable(getAnnotation(type));
    }
}

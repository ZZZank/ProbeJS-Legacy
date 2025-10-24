package zzzank.probejs.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unchecked")
public interface Cast {
    static <T> T to(Object obj) {
        return (T) obj;
    }

    @Nullable
    static <T> T inst(Object value, Class<T> type) {
        if (type.isInstance(value)) {
            return to(value);
        }
        return null;
    }

    @NotNull
    static <T> Optional<T> instOpt(Object value, Class<T> type) {
        return Optional.ofNullable(inst(value, type));
    }
}

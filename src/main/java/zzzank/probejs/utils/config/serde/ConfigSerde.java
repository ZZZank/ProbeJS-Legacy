package zzzank.probejs.utils.config.serde;

import org.jetbrains.annotations.NotNull;

/**
 * @param <T> object type at runtime
 * @author ZZZank
 */
public interface ConfigSerde<I, T> {

    @NotNull
    I toJson(@NotNull T value);

    @NotNull
    T fromJson(@NotNull I json);
}

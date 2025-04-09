package zzzank.probejs.utils.config.serde;

import org.jetbrains.annotations.NotNull;

/**
 * @param <I> intermediate object type
 * @param <T> object type at runtime
 * @author ZZZank
 */
public interface ConfigSerde<I, T> {

    @NotNull
    I serialize(@NotNull T value);

    @NotNull
    T deserialize(@NotNull I json);
}

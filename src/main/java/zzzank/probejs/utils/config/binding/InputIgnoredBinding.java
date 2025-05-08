package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class InputIgnoredBinding<T> extends DefaultBinding<T> {
    public InputIgnoredBinding(
        @NotNull T defaultValue,
        @NotNull Class<T> defaultType,
        @NotNull String name
    ) {
        super(defaultValue, defaultType, name);
    }

    @Override
    public @NotNull T get() {
        return defaultValue;
    }

    public @NotNull T receivedInput() {
        return super.get();
    }
}

package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;

/**
 * @author ZZZank
 */
public class DefaultBinding<T> extends BindingBase<T> {

    private T value;

    public DefaultBinding(@NotNull T defaultValue, @NotNull Class<T> defaultType, @NotNull String name) {
        super(defaultValue, defaultType, name);
        this.value = defaultValue; // null check done by super(...)
    }

    @Override
    public @NotNull T get() {
        return value;
    }

    @Override
    protected void setImpl(T value) {
        this.value = value;
    }
}

package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.config.report.ConfigReport;
import zzzank.probejs.utils.config.report.ReadOnlyError;

/**
 * @author ZZZank
 */
public class ReadOnlyBinding<T> extends BindingBase<T> {

    public ReadOnlyBinding(@NotNull T defaultValue, @NotNull Class<T> defaultType, @NotNull String name) {
        super(defaultValue, defaultType, name);
    }

    @Override
    public @NotNull T get() {
        return defaultValue;
    }

    @Override
    protected void setImpl(T value) {
    }

    @Override
    public ConfigReport validate(T value) {
        return new ReadOnlyError(name);
    }
}

package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.ConfigReport;
import zzzank.probejs.utils.config.report.NoError;
import zzzank.probejs.utils.config.report.NullValueError;
import zzzank.probejs.utils.config.report.WrappedException;
import zzzank.probejs.utils.config.report.holder.AccessResult;

/**
 * @author ZZZank
 */
public abstract class BindingBase<T> implements ConfigBinding<T> {

    @NotNull
    protected final T defaultValue;
    @NotNull
    protected final Class<T> defaultType;
    @NotNull
    protected final String name;

    protected BindingBase(@NotNull T defaultValue, @NotNull Class<T> defaultType, @NotNull String name) {
        this.defaultValue = Asser.tNotNull(defaultValue, "defaultValue");
        this.defaultType = Asser.tNotNull(defaultType, "defaultType");
        this.name = Asser.tNotNull(name, "name");
    }

    @Override
    public @NotNull T getDefault() {
        return defaultValue;
    }

    @Override
    public @NotNull Class<T> getDefaultType() {
        return defaultType;
    }

    @Override
    public @NotNull AccessResult<T> set(T value) {
        val validated = validate(value);
        if (validated.hasError()) {
            return AccessResult.noValue(validated);
        }
        try {
            setImpl(value);
            return AccessResult.none();
        } catch (Exception e) {
            return AccessResult.noValue(new WrappedException(e));
        }
    }

    abstract protected void setImpl(T value);

    public ConfigReport validate(T value) {
        if (value == null) {
            return new NullValueError(name);
        }
        return NoError.INSTANCE;
    }
}

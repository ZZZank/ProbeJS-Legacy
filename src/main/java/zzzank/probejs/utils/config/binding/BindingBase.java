package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.ConfigReport;
import zzzank.probejs.utils.config.report.NoError;
import zzzank.probejs.utils.config.report.NullValueError;
import zzzank.probejs.utils.config.report.WrappedException;

import java.util.Objects;

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
        this.defaultValue = Objects.requireNonNull(defaultValue);
        this.defaultType = Asser.tNotNull(defaultType, "defaultType");
        this.name = Objects.requireNonNull(name);
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
    public @NotNull ConfigReport set(T value) {
        val validated = validate(value);
        if (validated.hasError()) {
            return validated;
        }
        try {
            setImpl(value);
        } catch (Exception e) {
            reset();
            return new WrappedException(e);
        }
        return NoError.INSTANCE;
    }

    abstract protected void setImpl(T value);

    public ConfigReport validate(T value) {
        if (value == null) {
            return new NullValueError(name);
        }
        return NoError.INSTANCE;
    }
}

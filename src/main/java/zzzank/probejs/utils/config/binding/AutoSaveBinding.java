package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.AccessResult;
import zzzank.probejs.utils.config.struct.ConfigRoot;

/**
 * @author ZZZank
 */
public class AutoSaveBinding<T> implements ConfigBinding<T> {
    private final ConfigBinding<T> inner;
    private final ConfigRoot root;

    public AutoSaveBinding(ConfigBinding<T> inner, ConfigRoot root) {
        this.inner = Asser.tNotNull(inner, "inner binding");
        this.root = Asser.tNotNull(root, "config root");
    }

    @Override
    public @NotNull T getDefault() {
        return inner.getDefault();
    }

    @Override
    public @NotNull Class<T> getDefaultType() {
        return inner.getDefaultType();
    }

    @Override
    public @NotNull T get() {
        return inner.get();
    }

    @Override
    public AccessResult<T> getSafe() {
        return inner.getSafe();
    }

    @Override
    public @NotNull AccessResult<T> set(T value) {
        val result = inner.set(value);
        root.save();
        return result;
    }
}

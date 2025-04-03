package zzzank.probejs.utils.config.binding;

import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.config.report.WrappedException;
import zzzank.probejs.utils.config.report.holder.AccessResult;

/**
 * @author ZZZank
 */
public interface ConfigBinding<T> {

    @NotNull
    T getDefault();

    @NotNull
    Class<T> getDefaultType();

    @NotNull
    T get();

    default AccessResult<T> getSafe() {
        try {
            return AccessResult.onlyValue(get());
        } catch (Exception e) {
            return AccessResult.noValue(new WrappedException(e));
        }
    }

    @NotNull
    AccessResult<T> set(T value);
}

package zzzank.probejs.utils.config.report;

import lombok.experimental.UtilityClass;
import zzzank.probejs.utils.Asser;

import java.util.function.Supplier;

/**
 * @author ZZZank
 */
@UtilityClass
public class BuiltinResults {

    public <T> AccessResult<T> good(T value) {
        return AccessResult.onlyValue(value);
    }

    public <T> AccessResult<T> info(T value, Supplier<String> message) {
        return new CustomResult<>(value, AccessResult.ResultType.INFO, message);
    }

    public <T> AccessResult<T> warning(T value, Supplier<String> message) {
        return new CustomResult<>(value, AccessResult.ResultType.WARNING, message);
    }

    public <T> AccessResult<T> error(Supplier<String> message) {
        Asser.tNotNull(message, "message provider");
        return (AccessResult.NoValue<T>) message::get;
    }

    public <T> AccessResult<T> error(String message) {
        return error(() -> message);
    }

    public <T> AccessResult<T> readOnly(String name) {
        return error(() -> String.format("config entry '%s' is readonly", name));
    }

    public <T> AccessResult<T> nullValue(String name) {
        return error(() -> String.format("config entry '%s' received a null value", name));
    }

    public <T> AccessResult<T> outOfRange(String name, Object received, Object min, Object max) {
        return error(() -> String.format(
            "value %s for config entry '%s' not in range: [%s, %s]",
            received,
            name,
            min,
            max
        ));
    }

    public <T> AccessResult<T> exception(Throwable throwable) {
        return error(throwable::toString);
    }
}

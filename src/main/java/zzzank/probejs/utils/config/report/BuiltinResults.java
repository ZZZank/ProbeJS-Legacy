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
        return (AccessResult.OnlyValue<T>) () -> value;
    }

    public <T> AccessResult<T> info(T value, Supplier<String> message) {
        return new CustomResult<>(value, AccessResult.ResultType.INFO, message);
    }

    public <T> AccessResult<T> warning(T value, Supplier<String> message) {
        return new CustomResult<>(value, AccessResult.ResultType.WARNING, message);
    }

    public <T> AccessResult<T> error(Supplier<String> message) {
        Asser.tNotNull(message, "message provider");
        return errorImpl(message::get);
    }

    private <T> AccessResult<T> errorImpl(AccessResult.NoValue<T> message) {
        return message;
    }

    public <T> AccessResult<T> error(String message) {
        return errorImpl(() -> message);
    }

    public <T> AccessResult<T> readOnlyError(String name) {
        return errorImpl(() -> String.format("config entry '%s' is readonly", name));
    }

    public <T> AccessResult<T> nullValueError(String name) {
        return errorImpl(() -> String.format("config entry '%s' received a null value", name));
    }

    public <T> AccessResult<T> outOfRangeError(String name, Object received, Object min, Object max) {
        return errorImpl(() -> String.format(
            "value %s for config entry '%s' not in range: [%s, %s]",
            received,
            name,
            min,
            max
        ));
    }

    public <T> AccessResult<T> exception(Throwable throwable) {
        Asser.tNotNull(throwable, "throwable");
        return errorImpl(throwable::toString);
    }
}

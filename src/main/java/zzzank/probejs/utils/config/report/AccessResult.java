package zzzank.probejs.utils.config.report;

import lombok.val;
import zzzank.probejs.utils.Asser;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public interface AccessResult<T> {

    static <T> AccessResult<T> none() {
        return (AccessResult<T>) NoneResult.INSTANCE;
    }

    T value();

    ResultType type();

    String message();

    default boolean hasValue() {
        return value() != null;
    }

    default Optional<T> valueOptional() {
        return Optional.ofNullable(value());
    }

    default T valueOr(T fallback) {
        val value = value();
        return value == null ? fallback : value;
    }

    default T valueOrGet(Supplier<T> fallback) {
        Asser.tNotNull(fallback, "fallback value provider");
        val value = value();
        return value == null ? fallback.get() : value;
    }

    default Optional<String> messageOptional() {
        return Optional.ofNullable(message());
    }

    default boolean hasMessage() {
        return type() != ResultType.GOOD;
    }

    interface OnlyValue<T> extends AccessResult<T> {
        @Override
        default String message() {
            return null;
        }

        @Override
        default ResultType type() {
            return ResultType.GOOD;
        }
    }

    interface NoValue<T> extends AccessResult<T> {
        @Override
        default T value() {
            return null;
        }

        @Override
        default ResultType type() {
            return ResultType.ERROR;
        }
    }

    enum ResultType {
        GOOD,
        INFO,
        WARNING,
        ERROR
    }
}

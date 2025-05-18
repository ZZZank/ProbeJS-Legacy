package zzzank.probejs.utils.config.report;

import java.util.Optional;

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

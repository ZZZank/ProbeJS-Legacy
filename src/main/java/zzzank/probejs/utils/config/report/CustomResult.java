package zzzank.probejs.utils.config.report;

import zzzank.probejs.utils.Asser;

import java.util.function.Supplier;

/**
 * @author ZZZank
 */
final class CustomResult<T> implements AccessResult<T> {
    private final T value;
    private final ResultType type;
    private final Supplier<String> message;

    CustomResult(T value, ResultType type, Supplier<String> message) {
        this.value = value;
        this.type = Asser.tNotNull(type, "result type");
        this.message = Asser.tNotNull(message, "message provider");
    }

    @Override
    public T value() {
        return this.value;
    }

    @Override
    public ResultType type() {
        return this.type;
    }

    @Override
    public String message() {
        return this.message.get();
    }
}

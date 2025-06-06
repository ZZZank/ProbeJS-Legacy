package zzzank.probejs.utils.config.report;

import zzzank.probejs.utils.Asser;

import java.util.function.Supplier;

/**
 * @author ZZZank
 */
record AccessResultImpl<T>(
    T value,
    ResultType type,
    Supplier<String> messageProvider
) implements AccessResult<T> {

    AccessResultImpl(T value, ResultType type, Supplier<String> messageProvider) {
        this.value = value;
        this.type = Asser.tNotNull(type, "result type");
        this.messageProvider = Asser.tNotNull(messageProvider, "message provider");
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
        return this.messageProvider.get();
    }
}

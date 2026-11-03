package zzzank.probejs.utils.config.report;

import org.jetbrains.annotations.NotNull;
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

    AccessResultImpl {
        Asser.tNotNull(type, "result type");
        Asser.tNotNull(messageProvider, "message provider");
    }

    @Override
    public String message() {
        return this.messageProvider.get();
    }

    @Override
    public @NotNull String toString() {
        var message = message();
        return message == null ? type.toString() : type + ": " + message;
    }
}

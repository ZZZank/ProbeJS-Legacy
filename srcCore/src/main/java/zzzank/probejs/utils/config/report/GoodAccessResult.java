package zzzank.probejs.utils.config.report;

import org.jetbrains.annotations.NotNull;

record GoodAccessResult<T>(T value) implements AccessResult<T> {

    @Override
    public ResultType type() {
        return ResultType.GOOD;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public @NotNull String toString() {
        return this.type().toString();
    }
}

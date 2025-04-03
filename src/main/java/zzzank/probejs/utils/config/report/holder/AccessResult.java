package zzzank.probejs.utils.config.report.holder;

import zzzank.probejs.utils.config.report.ConfigReport;

import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank
 */
public interface AccessResult<T> {

    static <T> OnlyValue<T> onlyValue(T value) {
        return () -> value;
    }

    static <T> NoValue<T> noValue(List<ConfigReport> reports) {
        return () -> reports;
    }

    static <T> NoValue<T> noValue(ConfigReport report) {
        return noValue(Collections.singletonList(report));
    }

    static <T> AccessResult<T> none() {
        return (AccessResult<T>) NoneResult.INSTANCE;
    }

    T value();

    default boolean hasValue() {
        return value() != null;
    }

    List<ConfigReport> reports();

    default boolean hasReport() {
        return !reports().isEmpty();
    }

    interface OnlyValue<T> extends AccessResult<T> {
        @Override
        default List<ConfigReport> reports() {
            return Collections.emptyList();
        }
    }

    interface NoValue<T> extends AccessResult<T> {
        @Override
        default T value() {
            return null;
        }
    }
}

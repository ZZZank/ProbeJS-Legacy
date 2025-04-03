package zzzank.probejs.utils.config.report.holder;

import zzzank.probejs.utils.config.report.ConfigReport;

import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank
 */
final class NoneResult<T> implements AccessResult<T> {
    public static final NoneResult<?> INSTANCE = new NoneResult<>();

    private NoneResult() {
    }

    @Override
    public T value() {
        return null;
    }

    @Override
    public List<ConfigReport> reports() {
        return Collections.emptyList();
    }
}

package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.AccessResult;
import zzzank.probejs.utils.config.report.BuiltinResults;

/**
 * @author ZZZank
 */
public class RangedBinding<T extends Comparable<T>> extends DefaultBinding<T> {

    private final T min;
    private final T max;

    public RangedBinding(@NotNull T defaultValue, @NotNull Class<T> defaultType, @NotNull String name, @NotNull T min, @NotNull T max) {
        super(defaultValue, defaultType, name);
        this.min = Asser.tNotNull(min, "min");
        this.max = Asser.tNotNull(max, "max");
    }

    @Override
    public AccessResult<T> validate(T value) {
        val superReport = super.validate(value);
        if (superReport.hasMessage()) {
            return superReport;
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            return BuiltinResults.outOfRangeError(name, value, min, max);
        }
        return AccessResult.none();
    }
}

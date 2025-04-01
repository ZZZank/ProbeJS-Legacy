package zzzank.probejs.utils.config.binding;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.ConfigReport;

/**
 * @author ZZZank
 */
public interface ConfigBinding<T> {

    @NotNull
    T getDefault();

    @NotNull
    Class<T> getDefaultType();

    @NotNull
    T get();

    @NotNull
    ConfigReport set(T value);

    default ConfigReport reset() {
        return set(getDefault());
    }

    @SuppressWarnings("unchecked")
    default  <T_> Class<T_> clazzFromDefaultValue() {
        val def = getDefault();
        Asser.tNotNull(def, "config default value");
        return def instanceof Enum<?> e ? (Class<T_>) e.getDeclaringClass() : (Class<T_>) def.getClass();
    }
}

package zzzank.probejs.utils.config.struct;

import lombok.val;
import zzzank.probejs.utils.config.report.RuntimeError;
import zzzank.probejs.utils.config.report.holder.AccessResult;

import java.util.Collections;
import java.util.Map;

/**
 * @author ZZZank
 */
public interface ConfigCategory extends ConfigEntry<Map<String, ConfigEntry<?>>> {

    default ConfigEntry<?> getEntry(String path) {
        ConfigEntry<?> entry = this;
        for (val s : path.split("\\.")) {
            entry = entry.asCategory().get().get(s);
        }
        return entry;
    }

    @Override
    default AccessResult<Map<String, ConfigEntry<?>>> set(Map<String, ConfigEntry<?>> value) {
        return AccessResult.noValue(Collections.singletonList(new RuntimeError(
            "internal container of ConfigCategory should not be mutated externally")));
    }

    @Override
    default boolean isCategory() {
        return true;
    }
}

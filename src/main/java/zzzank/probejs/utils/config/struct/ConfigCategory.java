package zzzank.probejs.utils.config.struct;

import lombok.val;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.report.RuntimeError;
import zzzank.probejs.utils.config.report.holder.AccessResult;

import java.util.Map;

/**
 * @author ZZZank
 */
public interface ConfigCategory extends ConfigEntry<Map<String, ConfigEntry<?>>> {

    String PATH_SPLITTER = "\\.";

    default ConfigEntry<?> getEntry(String path) {
        ConfigEntry<?> entry = this;
        for (val s : path.split(PATH_SPLITTER)) {
            entry = entry.asCategory().get().get(s);
        }
        return entry;
    }

    @Override
    default AccessResult<Map<String, ConfigEntry<?>>> set(Map<String, ConfigEntry<?>> value) {
        return AccessResult.noValue(new RuntimeError(
            "internal container of ConfigCategory should not be mutated externally"));
    }

    @Override
    default boolean isCategory() {
        return true;
    }

    @Override
    default String path() {
        val parent = parent();
        return parent == null ? name() : parent.path() + '.' + name();
    }

    default ConfigEntryBuilder<Void> define(String name) {
        Asser.t(name.indexOf('.') < 0, "");
        return new ConfigEntryBuilder<>(this, name);
    }

    <T> ConfigEntry<T> register(ConfigEntry<T> entry);
}

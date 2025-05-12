package zzzank.probejs.utils.config.struct;

import lombok.val;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.report.RuntimeError;
import zzzank.probejs.utils.config.report.holder.AccessResult;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author ZZZank
 */
public interface ConfigCategory extends ConfigEntry<Map<String, ConfigEntry<?>>> {

    char PATH_SPLITTER_CHAR = '.';

    String PATH_SPLITTER = "\\.";

    default ConfigEntry<?> getEntry(String path) {
        if (path.isEmpty()) {
            return this;
        }
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
        return new ConfigEntryBuilder<>(this, name);
    }

    <T extends ConfigEntry<?>> T register(T entry);

    /// create an empty sub category
    default ConfigCategory subCategory(String name, Supplier<Map<String, ConfigEntry<?>>> mapStructure) {
        return register(new ConfigCategoryImpl(name, mapStructure, new ConfigProperties(), this));
    }

    /// create an empty sub category
    default ConfigCategory subCategory(String name) {
        return subCategory(name, LinkedHashMap::new);
    }
}

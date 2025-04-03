package zzzank.probejs.utils.config;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.report.holder.AccessResult;

import java.util.List;

/**
 * @author ZZZank
 */
public class ConfigEntry<T> {

    public final ConfigImpl source;
    public final String namespace;
    public final String name;

    public final ConfigBinding<T> binding;
    public final ConfigProperties properties;

    public ConfigEntry(
        ConfigImpl source,
        String namespace,
        String name,
        ConfigBinding<T> binding,
        ConfigProperties properties
    ) {
        this.source = Asser.tNotNull(source, "source");
        this.name = Asser.tNotNull(name, "name");
        this.binding = Asser.tNotNull(binding, "defaultValue");
        this.namespace = Asser.tNotNull(namespace, "namespace");
        this.properties = Asser.tNotNull(properties, "properties");
    }

    /**
     * `null` will be redirected to default value
     */
    public AccessResult<T> set(T value) {
        val report = setNoSave(value);
        source.save();
        return report;
    }

    public AccessResult<T> setNoSave(T value) {
        val result = binding.set(value);
        if (result.hasReport()) {
            ProbeJS.LOGGER.error("error when trying to set value for config entry '{}': {}", name, result.reports());
        }
        return result;
    }

    @NotNull
    public T get() {
        return binding.get();
    }

    @NotNull
    public T getDefault() {
        return binding.getDefault();
    }

    public <T_> T_ getProp(ConfigProperty<T_> property) {
        return properties.getOrDefault(property);
    }

    public List<String> getComments() {
        return getProp(ConfigProperty.COMMENTS);
    }

    public boolean readOnly() {
        return this.binding instanceof ReadOnlyBinding<?>;
    }

    public String path() {
        return source.stripNamespace(namespace, name);
    }
}

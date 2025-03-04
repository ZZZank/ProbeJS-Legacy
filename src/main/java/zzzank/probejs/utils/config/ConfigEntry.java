package zzzank.probejs.utils.config;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.report.ConfigReport;
import zzzank.probejs.utils.config.serde.ConfigSerde;

import java.util.List;

/**
 * @author ZZZank
 */
public class ConfigEntry<T> {

    public final ConfigImpl source;
    public final String namespace;
    public final String name;

    public final ConfigSerde<T> serde;
    public final ConfigBinding<T> binding;
    public final ConfigProperties properties;

    public ConfigEntry(
        ConfigImpl source,
        String namespace,
        String name,
        ConfigSerde<T> serde,
        ConfigBinding<T> binding,
        ConfigProperties properties
    ) {
        this.source = Asser.tNotNull(source, "source");
        this.name = Asser.tNotNull(name, "name");
        this.serde = Asser.tNotNull(serde, "serde");
        this.binding = Asser.tNotNull(binding, "defaultValue");
        this.namespace = Asser.tNotNull(namespace, "namespace");
        this.properties = Asser.tNotNull(properties, "properties");
    }

    /**
     * `null` will be redirected to default value
     */
    public ConfigReport set(T value) {
        val report = setNoSave(value);
        source.save();
        return report;
    }

    public ConfigReport reset() {
        val report = binding.reset();
        source.save();
        return report;
    }

    public ConfigReport setNoSave(T value) {
        val report = binding.set(value);
        if (report.hasError()) {
            ProbeJS.LOGGER.error("error when trying to set value for config entry '{}'", name, report.asException());
        }
        return report;
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
}

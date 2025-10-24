package zzzank.probejs.utils.config.io;

import lombok.val;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;
import zzzank.probejs.utils.config.struct.ConfigEntry;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author ZZZank
 */
public abstract class SerdeHolder<I> {
    private final ConfigProperty<ConfigSerde<I, ?>> property;
    private final List<ConfigSerdeFactory<I>> factories;

    protected SerdeHolder(ConfigProperty<ConfigSerde<I, ?>> property) {
        this(property, new ArrayList<>());
    }

    protected SerdeHolder(ConfigProperty<ConfigSerde<I, ?>> property, List<ConfigSerdeFactory<I>> factories) {
        this.property = property;
        this.factories = factories;
    }

    public <F extends ConfigSerdeFactory<I>> F registerSerdeFactory(F factory) {
        Asser.tNotNull(factory, "factory");
        factories.add(0, factory);
        return factory;
    }

    public <T, S extends ConfigSerde<I, T>> ConfigSerdeFactory<I> registerDirectSerdeFactory(Type type, S serde) {
        Asser.tNotNull(type, "type");
        Asser.tNotNull(serde, "serde");
        return registerSerdeFactory((t) -> type.equals(t) ? serde : null);
    }

    public <T> ConfigSerde<I, T> getSerdeNullable(ConfigEntry<T> entry) {
        if (entry.properties().has(property)) {
            return entry.getProp((ConfigProperty<ConfigSerde<I, T>>) (Object) property)
                .orElseThrow(() -> new IllegalStateException(String.format(
                    "config entry '%s' has property '%s', but found null property value",
                    entry.path(),
                    property
                )));
        }
        val type = entry.binding().getDefaultType();
        val created = factories.stream()
            .map(f -> f.getSerde(type))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        if (created != null) {
            entry.properties().put(property, created);
        }
        return (ConfigSerde<I, T>) created;
    }

    public <T> ConfigSerde<I, T> getSerde(ConfigEntry<T> entry) {
        val got = getSerdeNullable(entry);
        if (got == null) {
            throw new IllegalStateException(String.format(
                "no serde available for config entry '%s' with type '%s'",
                entry.path(),
                entry.binding().getDefaultType()
            ));
        }
        return got;
    }
}

package zzzank.probejs.utils.config.serde.holder;

import lombok.val;
import zzzank.probejs.utils.Cast;
import zzzank.probejs.utils.config.report.holder.AccessResult;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;
import zzzank.probejs.utils.config.struct.ConfigEntry;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @param <I> intermediate object type, used by {@link zzzank.probejs.utils.config.serde.ConfigSerde}
 * @author ZZZank
 */
public interface SerdeHolder<I> {

    default <T> ConfigSerde<I, T> getSerde(Class<T> type) {
        val known = getKnownSerdes().get(type);
        if (known != null) {
            return Cast.to(known);
        }
        val created = getSerdeFactories()
            .stream()
            .map(f -> f.getSerde(type))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
        if (created != null) {
            registerSerde(type, created);
        }
        return created;
    }

    default <T> ConfigSerde<I, T> serdeByEntryRequired(ConfigEntry<T> entry) {
        val serde = getSerde(entry.binding().getDefaultType());
        if (serde == null) {
            throw new IllegalStateException(String.format(
                "No ConfigSerde available for config '%s' with type '%s'",
                entry.path(),
                entry.binding().getDefaultType().getName()
            ));
        }
        return serde;
    }

    <T, S extends ConfigSerde<I, T>> AccessResult<S> registerSerde(Class<T> type, S serde);

    <F extends ConfigSerdeFactory<I>> AccessResult<F> registerSerdeFactory(F factory);

    Map<Class<?>, ConfigSerde<I, ?>> getKnownSerdes();

    /**
     * There's no guarantee in the order of factories
     */
    List<ConfigSerdeFactory<I>> getSerdeFactories();
}

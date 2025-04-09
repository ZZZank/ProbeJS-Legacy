package zzzank.probejs.utils.config.serde;

import java.lang.reflect.Type;

/**
 * @param <I> intermediate object type, used by {@link zzzank.probejs.utils.config.serde.ConfigSerde}
 * @author ZZZank
 */
public interface ConfigSerdeFactory<I> {

    <T> ConfigSerde<I, T> getSerde(Class<T> type);

    default ConfigSerde<I, ?> getSerde(Type type) {
        throw new IllegalStateException("no impl yet");
    }
}

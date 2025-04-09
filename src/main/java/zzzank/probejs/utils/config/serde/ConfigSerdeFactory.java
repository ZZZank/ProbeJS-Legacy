package zzzank.probejs.utils.config.serde;

import java.lang.reflect.Type;

/**
 * @author ZZZank
 */
public interface ConfigSerdeFactory<I> {

    <T> ConfigSerde<I, T> getSerde(Class<T> type);

    default ConfigSerde<I, ?> getSerde(Type type) {
        throw new IllegalStateException("no impl yet");
    }
}

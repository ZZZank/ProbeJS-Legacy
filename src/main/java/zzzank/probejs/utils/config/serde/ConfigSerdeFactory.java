package zzzank.probejs.utils.config.serde;

import java.lang.reflect.Type;

/**
 * @author ZZZank
 */
public interface ConfigSerdeFactory {

    <T> ConfigSerde<T> getSerde(Class<T> type);

    default ConfigSerde<?> getSerde(Type type) {
        throw new IllegalStateException("no impl yet");
    }
}

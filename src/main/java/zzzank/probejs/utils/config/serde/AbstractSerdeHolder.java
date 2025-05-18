package zzzank.probejs.utils.config.serde;

import zzzank.probejs.utils.config.report.BuiltinResults;
import zzzank.probejs.utils.config.report.AccessResult;

import java.util.*;

/**
 * @author ZZZank
 */
public abstract class AbstractSerdeHolder<I> implements ConfigSerdeHolder<I> {

    protected final List<ConfigSerdeFactory<I>> factories;
    protected final Map<Class<?>, ConfigSerde<I, ?>> serdes;

    protected AbstractSerdeHolder(List<ConfigSerdeFactory<I>> factories, Map<Class<?>, ConfigSerde<I, ?>> serdes) {
        this.factories = factories;
        this.serdes = serdes;
    }

    protected AbstractSerdeHolder() {
        this(new ArrayList<>(), new HashMap<>());
    }

    @Override
    public <T, S extends ConfigSerde<I, T>> AccessResult<S> registerSerde(Class<T> type, S serde) {
        if (serde == null) {
            return BuiltinResults.nullValueError("serde");
        } else if (type == null) {
            return BuiltinResults.nullValueError("type");
        }
        serdes.put(type, serde);
        return BuiltinResults.good(serde);
    }

    @Override
    public <F extends ConfigSerdeFactory<I>> AccessResult<F> registerSerdeFactory(F factory) {
        if (factory == null) {
            return BuiltinResults.nullValueError("serde factory");
        }
        factories.add(0, factory);
        return BuiltinResults.good(factory);
    }

    @Override
    public Map<Class<?>, ConfigSerde<I, ?>> getKnownSerdes() {
        return Collections.unmodifiableMap(serdes);
    }

    @Override
    public List<ConfigSerdeFactory<I>> getSerdeFactories() {
        return Collections.unmodifiableList(factories);
    }
}

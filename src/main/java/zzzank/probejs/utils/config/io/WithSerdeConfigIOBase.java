package zzzank.probejs.utils.config.io;

import zzzank.probejs.utils.config.report.NullValueError;
import zzzank.probejs.utils.config.report.holder.AccessResult;
import zzzank.probejs.utils.config.serde.ConfigSerde;
import zzzank.probejs.utils.config.serde.ConfigSerdeFactory;

import java.util.*;

/**
 * @author ZZZank
 */
public abstract class WithSerdeConfigIOBase<I> implements WithSerdeConfigIO<I> {
    protected final List<ConfigSerdeFactory<I>> factories;
    private final Map<Class<?>, ConfigSerde<I, ?>> serdes;

    protected WithSerdeConfigIOBase(List<ConfigSerdeFactory<I>> factories, Map<Class<?>, ConfigSerde<I, ?>> serdes) {
        this.factories = factories;
        this.serdes = serdes;
    }

    protected WithSerdeConfigIOBase() {
        this(new ArrayList<>(), new HashMap<>());
    }

    @Override
    public <T, S extends ConfigSerde<I, T>> AccessResult<S> registerSerde(Class<T> type, S serde) {
        if (serde == null) {
            return AccessResult.noValue(Collections.singletonList(new NullValueError("serde")));
        } else if (type == null) {
            return AccessResult.noValue(Collections.singletonList(new NullValueError("type")));
        }
        serdes.put(type, serde);
        return AccessResult.onlyValue(serde);
    }

    @Override
    public <F extends ConfigSerdeFactory<I>> AccessResult<F> registerSerdeFactory(F factory) {
        if (factory == null) {
            return AccessResult.noValue(Collections.singletonList(new NullValueError("serde factory")));
        }
        factories.add(0, factory);
        return AccessResult.onlyValue(factory);
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

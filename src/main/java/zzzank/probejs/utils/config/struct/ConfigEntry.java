package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.binding.ReadOnlyBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.prop.ConfigProperty;
import zzzank.probejs.utils.config.report.AccessResult;

import java.util.Optional;

/**
 * @author ZZZank
 */
public interface ConfigEntry<T> {

    /// basic action

    /// The name of this config entry. For [ConfigRoot], it will always return an empty string
    ///
    /// For complete path of this config entry, use [#path()]
    String name();

    /// Get the config value of this config entry
    ///
    /// @return the config value of this config entry, or `null` if exceptions happened when getting config value
    default T get() {
        return binding().get();
    }

    default T getDefault() {
        return binding().getDefault();
    }

    default AccessResult<T> getSafe() {
        return binding().getSafe();
    }

    default boolean isReadOnly() {
        return binding() instanceof ReadOnlyBinding<?>;
    }

    ConfigBinding<T> binding();

    default AccessResult<T> set(T value) {
        return binding().set(value);
    }

    ConfigProperties properties();

    default <P> Optional<P> getProp(ConfigProperty<P> property) {
        return properties().get(property);
    }

    /// The parent config category holding this config entry
    ///
    /// this method returns `null` when and only when this config entry is an [ConfigRoot]
    ConfigCategory parent();

    /// @return `true` if and only if this config entry is a [ConfigCategory]
    default boolean isCategory() {
        return false;
    }

    /// get a [ConfigCategory] cast directly from the caller config entry
    ///
    /// @throws ClassCastException if the caller config entry is not a [ConfigCategory]
    default ConfigCategory asCategory() {
        return (ConfigCategory) this;
    }

    /// @return `true` if and only if this config entry is a [ConfigRoot]
    default boolean isRoot() {
        return false;
    }

    /// get a [ConfigRoot] cast directly from the caller config entry
    ///
    /// @throws ClassCastException if the caller config entry is not a [ConfigRoot]
    default ConfigRoot asRoot() {
        return (ConfigRoot) this;
    }

    default ConfigRoot getRoot() {
        return this instanceof ConfigRoot root ? root : this.parent().getRoot();
    }

    /// The path from the root of the config to this config entry. For [ConfigRoot], it will always return an empty string
    ///
    /// For name of this config entry, use [#name()]
    default String path() {
        return parent().path() + '.' + name();
    }
}


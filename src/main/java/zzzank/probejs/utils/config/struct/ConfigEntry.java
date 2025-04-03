package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.config.binding.ConfigBinding;
import zzzank.probejs.utils.config.prop.ConfigProperties;
import zzzank.probejs.utils.config.report.holder.AccessResult;

/**
 * @author ZZZank
 */
public interface ConfigEntry<T> {

    /// basic action

    /// The name of this config entry.
    ///
    /// For complete path of this config entry, use {@link #path()}
    String name();

    /// Get the config value of this config entry
    ///
    /// @return the config value of this config entry, or `null` if exceptions happened when getting config value
    default T get() {
        return binding().get();
    }

    default AccessResult<T> getSafe() {
        return binding().getSafe();
    }

    ConfigBinding<T> binding();

    default AccessResult<T> set(T value) {
        return binding().set(value);
    }

    ConfigProperties properties();

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

    default String path() {
        return parent().path() + '.' + name();
    }
}


package zzzank.probejs.utils.config.struct;

import zzzank.probejs.utils.config.io.ConfigIO;

/**
 * @author ZZZank
 */
public interface ConfigRoot extends ConfigCategory {

    ConfigIO io();

    @Override
    default boolean isRoot() {
        return true;
    }

    @Override
    default ConfigCategory parent() {
        return null;
    }

    @Override
    default String path() {
        return "";
    }
}

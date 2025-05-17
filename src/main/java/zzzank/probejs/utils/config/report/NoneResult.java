package zzzank.probejs.utils.config.report;

/**
 * @author ZZZank
 */
enum NoneResult implements AccessResult.OnlyValue<Object> {
    INSTANCE;

    @Override
    public Object value() {
        return null;
    }
}

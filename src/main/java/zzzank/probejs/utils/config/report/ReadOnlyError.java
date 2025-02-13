package zzzank.probejs.utils.config.report;

/**
 * @author ZZZank
 */
public class ReadOnlyError extends RuntimeError {

    public ReadOnlyError(String name) {
        super(String.format("config entry '%s' is readonly", name));
    }
}

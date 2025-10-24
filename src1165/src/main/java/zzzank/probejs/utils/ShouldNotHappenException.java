package zzzank.probejs.utils;

/**
 * @author ZZZank
 */
public class ShouldNotHappenException extends RuntimeException {
    public ShouldNotHappenException(String message) {
        super(message);
    }

    public ShouldNotHappenException(Throwable cause) {
        super(cause);
    }
}

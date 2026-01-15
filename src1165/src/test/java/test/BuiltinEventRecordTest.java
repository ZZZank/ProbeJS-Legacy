package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.features.kubejs.BuiltinEventRecord;

/**
 * @author ZZZank
 */
public class BuiltinEventRecordTest {

    static {
        InitFMLPathsTest.init();
    }

    @Test
    public void test() {
        Assertions.assertFalse(BuiltinEventRecord.RECORDS.isEmpty());
    }
}

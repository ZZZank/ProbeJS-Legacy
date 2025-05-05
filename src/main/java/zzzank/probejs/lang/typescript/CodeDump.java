package zzzank.probejs.lang.typescript;

import zzzank.probejs.api.output.AutoSplitPackagedWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.transpiler.Transpiler;

/**
 * @author ZZZank
 */
public class CodeDump {
    public static final String SIMPLE_PACKAGE = "simple_package_classes";

    public final ClassRegistry registry = new ClassRegistry();
    public final Transpiler transpiler = new Transpiler();
    public final TSFileWriter writer = new AutoSplitPackagedWriter(
        2,
        Integer.MAX_VALUE,
        200,
        SIMPLE_PACKAGE
    );
}

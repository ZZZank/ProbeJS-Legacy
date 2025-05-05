package zzzank.probejs.lang.typescript;

import zzzank.probejs.api.output.AutoSplitPackagedWriter;
import zzzank.probejs.api.output.TSFileWriter;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;

import java.util.Map;

/**
 * @author ZZZank
 */
public class CodeDump {
    public static final String SIMPLE_PACKAGE = "simple_package_classes";

    public final Transpiler transpiler = new Transpiler();
    public final TSFileWriter writer = new AutoSplitPackagedWriter(
        2,
        Integer.MAX_VALUE,
        200,
        SIMPLE_PACKAGE
    );
    private Map<ClassPath, ClassDecl> classDecls = null;
}

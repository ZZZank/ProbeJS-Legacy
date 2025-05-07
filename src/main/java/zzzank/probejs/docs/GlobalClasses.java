package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.TypeSpecificFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;
import zzzank.probejs.lang.typescript.code.type.utility.TSUtilityType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Collections;

/**
 * @author ZZZank
 */
public class GlobalClasses implements ProbeJSPlugin {
    public static final JSPrimitiveType CLASS_PATH = Types.primitive("ClassPath");
    public static final JSPrimitiveType JAVA_CLASS_PATH = Types.primitive("JavaClassPath");
    public static final JSPrimitiveType TS_CLASS_PATH = Types.primitive("TSClassPath");
    public static final JSPrimitiveType GLOBAL_CLASSES = Types.primitive("GlobalClasses");
    public static final JSPrimitiveType LOAD_CLASSES = Types.primitive("LoadClass");
    public static final TSClassType J_CLASS = Types.type(ClassPath.fromRaw("zzzank.probejs.docs.duck.JClass"));

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val converter = scriptDump.transpiler.typeConverter;

        val T = Types.generic("T", CLASS_PATH);
        val TRaw = Types.generic("T");

        val paths = Types.object();
        for (val clazz : scriptDump.recordedClasses) {
            val path = clazz.classPath;
            val typeOf = Types.typeOf(clazz.classPath);
            //original
            paths.member(clazz.getOriginal().getName(), typeOf);
            //probejs style import
            paths.member(path.getTSPath(), typeOf);
        }

        val classPathTemplate = Types.primitive(String.format("`%s${string}`", ClassPath.TS_PATH_PREFIX));
        scriptDump.addGlobal(
            "load_class",
            Statements.type(GLOBAL_CLASSES.content, paths.build())
                .typeFormat(BaseType.FormatType.RETURN)
                .build(),
            new TypeDecl(CLASS_PATH.content, Types.STRING.and(Types.primitive("keyof GlobalClasses"))),
            new TypeDecl(JAVA_CLASS_PATH.content, TSUtilityType.exclude(CLASS_PATH, classPathTemplate)),
            new TypeDecl(TS_CLASS_PATH.content, TSUtilityType.extract(CLASS_PATH, classPathTemplate)),
            Statements.type("AttachJClass", TRaw.and(J_CLASS.withParams(TSUtilityType.instanceType(TRaw))))
                .symbolVariables(Collections.singletonList(TRaw))
                .exportDecl(false)
                .build(),
            new TypeDecl(
                LOAD_CLASSES.content,
                Collections.singletonList(T),
                Types.ternary(
                    T.symbol,
                    T.extend,
                    Types.format("AttachJClass<%s[T]>", GLOBAL_CLASSES),
                    Types.NEVER
                )
            )
        );
    }

    @Override
    public void modifyFiles(TypeSpecificFiles files) {
        files.addDumpSpecificFile(
            J_CLASS.classPath,
            Statements.clazz(J_CLASS.classPath.getName())
                .abstractClass()
                .typeVariables("T")
                .field("prototype", Types.NULL)
                .field("__javaObject__", Types.type(Class.class).withParams("T"))
                .build()
        );
    }
}

package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
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
    public static final JSPrimitiveType GLOBAL_CLASSES = Types.primitive("GlobalClasses");
    public static final JSPrimitiveType LOAD_CLASS = Types.primitive("LoadClass");
    public static final TSClassType J_CLASS = Types.type(ClassPath.ofArtificial("zzzank.probejs.generated.JClass"));

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val T = Types.generic("T");

        val paths = Types.object();
        for (val clazz : scriptDump.recordedClasses) {
            val path = clazz.classPath;
            paths.member(path.getOriginalName(), Types.typeOf(path));
        }

        scriptDump.addGlobal(
            "load_class",
            // export type GlobalClasses = { ... }
            Statements.type(GLOBAL_CLASSES.content, paths.build())
                .typeFormat(BaseType.FormatType.RETURN)
                .build(),
            // type AttachJClass<T> = T & JClass<InstanceType<T>>
            Statements.type("AttachJClass", T.and(J_CLASS.withParams(TSUtilityType.instanceType(T))))
                .symbolVariables(Collections.singletonList(T))
                .exportDecl(false)
                .build(),
            // export type LoadClass<T> = T extends (keyof GlobalClasses) ? AttachJClass<GlobalClasses[T]> : never
            new TypeDecl(
                LOAD_CLASS.content,
                Collections.singletonList(T),
                Types.ternary(
                    "T",
                    Types.keyof(GLOBAL_CLASSES),
                    Types.format("AttachJClass<%s[T]>", GLOBAL_CLASSES),
                    Types.NEVER
                )
            )
        );
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val jClassDecl = Statements.clazz(J_CLASS.classPath.getSimpleName())
            .abstractClass()
            .typeVariables("T")
            .field("prototype", Types.NULL)
            .field("__javaObject__", Types.type(Class.class).withParams("T"))
            .build();
        jClassDecl.bodyCode.add(Types.primitive("[Symbol.hasInstance]: (o: any) => o is T;"));
        files.requestOrCreate(J_CLASS.classPath).addCodes(jClassDecl);
    }
}

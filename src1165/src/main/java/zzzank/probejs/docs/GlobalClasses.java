package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.lang.typescript.code.type.utility.TSUtilityType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank
 */
public class GlobalClasses implements ProbeJSPlugin {
    public static final JSPrimitiveType GLOBAL_CLASSES = Types.primitive("GlobalClasses");
    public static final JSPrimitiveType LOAD_CLASS = Types.primitive("LoadClass");
    public static final JSPrimitiveType J_CLASS = Types.primitive("JClass");
    public static final JSPrimitiveType ATTACH_J_CLASS = Types.primitive("AttachJClass");

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
            // export type JClass<T> = { ... }
            Statements.type()
                .name(J_CLASS.content)
                .symbolVariables(List.of(T))
                .type(Types.object()
                    .member("__javaObject__", Types.type(Class.class).withParams("T"))
                    .rawNameMember("[Symbol.hasInstance]", Types.primitive("(o: any) => o is T"))
                    .build())
                .build(),
            // type AttachJClass<T> = T & JClass<InstanceType<T>>
            Statements.type(ATTACH_J_CLASS.content, T.and(J_CLASS.withParams(TSUtilityType.instanceType(T))))
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
                    ATTACH_J_CLASS.withParams(GLOBAL_CLASSES.access(T)),
                    Types.NEVER
                )
            )
        );
    }
}

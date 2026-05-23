package zzzank.probejs.docs;

import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.List;

/**
 * @author ZZZank
 */
public class GlobalClasses implements ProbeJSPlugin {
    public static final JSPrimitiveType LOAD_CLASS = Types.primitive("LoadClass");
    public static final JSPrimitiveType J_CLASS = Types.primitive("JClass");

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        var dualStringTuple = Types.tuple()
            .member("head", Types.STRING)
            .member("tail", Types.STRING)
            .build();

        scriptDump.addGlobal(
            "load_class",
            // export type JClass<T> = abstract new (...args: any) => T
            Statements.type()
                .name(J_CLASS.content)
                .symbolVariables(List.of(Types.generic("T")))
                .type(Types.primitive("abstract new (...args: any) => T"))
                .build(),
            // type JoinPrefix<PREFIX extends string, PARTS extends [string, string]> = [`${PREFIX}.${PARTS[0]}`, PARTS[1]]
            Statements.type()
                .name("JoinPrefix")
                .symbolVariables(
                    Types.generic("PREFIX", Types.STRING),
                    Types.generic("PARTS", dualStringTuple)
                )
                .type(Types.primitive("[`${PREFIX}.${PARTS[0]}`, PARTS[1]]"))
                .exportDecl(false)
                .build(),
            // type SplitJavaClassPath<T extends string> = T extends `${infer HEAD}.${infer REST}`
            //    ? JoinPrefix<HEAD, SplitJavaClassPath<REST>>
            //    : ["", T]
            Statements.type()
                .name("SplitJavaClassPath")
                .symbolVariables(Types.generic("T", Types.STRING))
                .type(Types.primitive("""
                    T extends `${infer HEAD}.${infer REST}`
                        ? JoinPrefix<HEAD, SplitJavaClassPath<REST>>
                        : ["", T]"""))
                .exportDecl(false)
                .build(),
            // type LoadByModule<MODULES, PARTS extends [string, string]> = PARTS[0] extends keyof MODULES
            //     ? `$${PARTS[1]}` extends keyof MODULES[PARTS[0]] ? MODULES[PARTS[0]][`$${PARTS[1]}`] : never
            //     : never
            Statements.type()
                .name("LoadByModule")
                .symbolVariables(Types.generic("MODULES"), Types.generic("PARTS", dualStringTuple))
                .type(Types.primitive("""
                    PARTS[0] extends keyof MODULES
                         ? `$${PARTS[1]}` extends keyof MODULES[PARTS[0]] ? MODULES[PARTS[0]][`$${PARTS[1]}`] : never
                         : never"""))
                .exportDecl(false)
                .build(),
            // export type LoadClass<T extends string> = LoadByModule<ProbeJS$$KnownModules, SplitJavaClassPath<T>>;
            new TypeDecl(
                LOAD_CLASS.content,
                List.of(Types.generic("T", Types.STRING)),
                Types.primitive("LoadByModule<ProbeJS$$KnownModules, SplitJavaClassPath<T>>")
            )
        );
    }
}

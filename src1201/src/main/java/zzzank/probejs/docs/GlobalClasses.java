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
        var TREE = Types.generic("TREE");
        var NAME = Types.generic("NAME", Types.STRING);
        var PATH = Types.generic("PATH", Types.STRING);

        scriptDump.addGlobal(
            "load_class",
            // export type JClass<T> = abstract new (...args: any) => T
            Statements.type()
                .name(J_CLASS.content)
                .symbolVariables(List.of(Types.generic("T")))
                .type(Types.primitive("abstract new (...args: any) => T"))
                .build(),
            // type ResolveClassInTree<TREE, NAME extends string> = NAME extends `${infer PKG}.${infer REST}`
            //     ? PKG extends keyof TREE ? ResolveClassInTree<TREE[PKG], REST> : never
            //     : `$${NAME}` extends keyof TREE ? TREE[`$${NAME}`] : never;
            new TypeDecl(
                false,
                "ResolveClassInTree",
                List.of(TREE, NAME),
                Types.ternary(
                    "NAME",
                    Types.primitive("`${infer PKG}.${infer REST}`"),
                    // true: PKG extends keyof TREE ? ResolveClassInTree<TREE[PKG], REST> : never
                    Types.ternary(
                        "PKG",
                        Types.keyof(TREE),
                        Types.primitive("ResolveClassInTree").withParams(
                            TREE.access(Types.primitive("PKG")),
                            Types.primitive("REST")
                        ),
                        Types.NEVER
                    ),
                    // false: `$${NAME}` extends keyof TREE ? TREE[`$${NAME}`] : never
                    Types.format(
                        "%s extends %s ? %s : %s",
                        Types.primitive("`$${NAME}`"),
                        Types.keyof(TREE),
                        TREE.access(Types.primitive("`$${NAME}`")),
                        Types.NEVER
                    )
                ),
                BaseType.FormatType.RETURN
            ),
            // export type LoadClass<PATH extends string> = ResolveClassInTree<typeof import("index"), PATH>;
            new TypeDecl(
                LOAD_CLASS.content,
                List.of(PATH),
                Types.primitive("ResolveClassInTree").withParams(
                    Types.primitive("typeof import(\"java:index\")"),
                    PATH
                )
            )
        );
    }
}

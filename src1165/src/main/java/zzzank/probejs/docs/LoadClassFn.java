package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.docs.assignments.SpecialTypes;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @author ZZZank
 */
public class LoadClassFn implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        var T = Types.generic("T", SpecialTypes.dot("ClassNames"));
        val javaFn = Statements
            .func("java")
            .variable(T)
            .param("classPath", T)
            .returnType(Types.primitive("LoadClass").withParams("T"))
            .build();

        val requireFn = Statements
            .func("require")
            .param("name", Types.STRING)
            .returnType(Types.ANY)
            .build();
        requireFn.addComment(
            "@deprecated please use `java()` directly, ProbeJS adds TS path support for it.",
            "@see java"
        );

        scriptDump.addGlobal("java", javaFn, requireFn);
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        filter.denyFunction("java");
        filter.denyFunction("require");
    }
}

package zzzank.probejs.docs;

import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import lombok.val;
import zzzank.probejs.docs.assignments.SpecialTypes;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @author ZZZank
 */
public class LoadClassFn implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val requireFn = Statements
            .func("require")
            .param("name", Types.STRING)
            .returnType(Types.ANY)
            .build();
        requireFn.addComment("""
            @deprecated please use `Java.loadClass(...)` instead
            """);

        scriptDump.addGlobal("java", requireFn);
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val file = files.request(JavaWrapper.class);
        if (file == null) {
            return;
        }
        val decl = file.findCode(ClassDecl.class).orElse(null);
        if (decl == null) {
            return;
        }
        for (val method : decl.methods) {
            if ("loadClass".equals(method.name) || "tryLoadClass".equals(method.name)) {
                // loadClass<T extends Special.ClassNames>(className: T): LoadClass<T>
                // "T extends string" because we want T to capture the string literal
                val T = Types.generic("T", SpecialTypes.dot("ClassNames"));
                method.variableTypes.add(T);
                method.params.get(0).type = T;
                method.returnType = GlobalClasses.LOAD_CLASS.withParams(T);
            }
        }
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        filter.denyFunction("require");
    }
}

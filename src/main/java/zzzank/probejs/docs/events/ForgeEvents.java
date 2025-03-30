package zzzank.probejs.docs.events;


import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import lombok.val;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;
import zzzank.probejs.docs.GlobalClasses;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Map;

public class ForgeEvents implements ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        val file = globalClasses.get(ClassPath.fromJava(ForgeEventWrapper.class));
        file.declaration.addImport(ImportInfo.ofDefault(ClassPath.fromJava(Event.class)));
        file.declaration.addImport(ImportInfo.ofDefault(ClassPath.fromJava(GenericEvent.class)));
        val classDecl = file.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) {
            return;
        }

        for (val method : classDecl.methods) {
            if (method.name.equals("onEvent")) {
                method.variableTypes.add(Types.generic("T", Types.typeMaybeGeneric(Event.class)));
                method.params.get(0).type = GlobalClasses.J_CLASS.withParams("T");
                method.params.get(1).type = Types.lambda()
                    .param("event", Types.primitive("T"))
                    .build();
            } else if (method.name.equals("onGenericEvent")) {
                method.variableTypes.add(Types.generic("T", Types.typeMaybeGeneric(GenericEvent.class)));
                method.params.get(0).type = GlobalClasses.J_CLASS.withParams("T");
                method.params.get(1).type = GlobalClasses.J_CLASS.withParams(Types.ANY);
                method.params.get(2).type = Types.lambda()
                    .param("event", Types.primitive("T"))
                    .build();
            }
        }
    }
}

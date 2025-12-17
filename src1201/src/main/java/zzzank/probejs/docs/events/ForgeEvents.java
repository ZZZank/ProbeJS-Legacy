package zzzank.probejs.docs.events;


import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import lombok.val;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.GenericEvent;
import zzzank.probejs.docs.GlobalClasses;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.plugin.ProbeJSPlugin;

public class ForgeEvents implements ProbeJSPlugin {

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val file = files.request(ClassPath.ofJava(ForgeEventWrapper.class));
        file.declaration.addImport(ImportInfo.ofDefault(ClassPath.ofJava(Event.class)));
        file.declaration.addImport(ImportInfo.ofDefault(ClassPath.ofJava(GenericEvent.class)));
        file.declaration.addImport(ImportInfo.ofDefault(GlobalClasses.J_CLASS.classPath));
        val classDecl = file.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) {
            return;
        }

        for (val method : classDecl.methods) {
            if (method.name.equals("onEvent")) {
                method.variableTypes.add(Types.generic("T", Types.type(Event.class)));
                method.params.get(0).type = GlobalClasses.J_CLASS.withParams("T");
                method.params.get(1).type = Types.lambda()
                    .param("event", Types.primitive("T"))
                    .build();
            } else if (method.name.equals("onGenericEvent")) {
                method.variableTypes.add(Types.generic("T", Types.type(GenericEvent.class)));
                method.params.get(0).type = GlobalClasses.J_CLASS.withParams("T");
                method.params.get(1).type = GlobalClasses.J_CLASS.withParams(Types.ANY);
                method.params.get(2).type = Types.lambda()
                    .param("event", Types.primitive("T"))
                    .build();
            }
        }
    }
}

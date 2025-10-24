package zzzank.probejs.docs.events;


import dev.latvian.kubejs.script.ScriptType;
import lombok.val;
import net.minecraftforge.eventbus.api.Event;
import zzzank.probejs.docs.GlobalClasses;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.FunctionDeclaration;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.lang.typescript.code.type.utility.TSUtilityType;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Collections;
import java.util.Set;

public class ForgeEvents implements ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.STARTUP) {
            return;
        }

        val classArgOnEvent = buildOnForgeEvent(
            Types.generic("T", Types.typeOf(Event.class)),
            TSUtilityType.instanceType(Types.primitive("T"))
        )
            .build();
        val stringArgOnEvent = buildOnForgeEvent(
            Types.generic("T", GlobalClasses.JAVA_CLASS_PATH),
            TSUtilityType.instanceType(
                TSUtilityType.extract(
                    Types.format("%s[T]", GlobalClasses.GLOBAL_CLASSES),
                    Types.typeOf(Event.class)
                )
            )
        )
            .build();

        scriptDump.addGlobal("forge_events", classArgOnEvent, stringArgOnEvent);
    }

    private static FunctionDeclaration.Builder buildOnForgeEvent(TSVariableType variableT, TSParamType eventType) {
        return Statements
            .func("onForgeEvent")
            .variable(variableT)
            .param("target", variableT)
            .param(
                "handler",
                Types.lambda()
                    .param("event", eventType)
                    .build()
            );
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Collections.singleton(Event.class);
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        filter.denyFunction("onForgeEvent");
    }
}

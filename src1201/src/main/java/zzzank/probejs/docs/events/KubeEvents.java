package zzzank.probejs.docs.events;

import com.google.common.collect.ArrayListMultimap;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import lombok.val;
import zzzank.probejs.features.kesseractjs.TypeDescAdapter;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.features.kubejs.EventJSFilter;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.ts.FunctionDeclaration;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.*;
import java.util.function.BiPredicate;

public class KubeEvents implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val availableHandlers = ArrayListMultimap.<String, EventHandler>create();
        val filter = getDisabledEvents(scriptDump);
        val converter = scriptDump.transpiler.typeConverter;

        for (val entry : EventGroup.getGroups().entrySet()) {
            val name = entry.getKey();
            val group = entry.getValue();

            for (val handler : group.getHandlers().values()) {
                if (!handler.scriptTypePredicate.test(scriptDump.scriptType)
                    || filter.test(group, handler)) {
                    continue;
                }
                availableHandlers.put(name, handler);
            }
        }

        val codes = new ArrayList<Code>();
        for (val entry : availableHandlers.asMap().entrySet()) {
            val group = entry.getKey();
            val handlers = entry.getValue();

            val groupNamespace = new Wrapped.Namespace(group);
            for (val handler : handlers) {
                if (handler.extra != null) {
                    groupNamespace.addCode(formatEvent(converter, handler, true));
                    if (handler.extra.required) {
                        continue;
                    }
                }
                groupNamespace.addCode(formatEvent(converter, handler, false));
            }
            codes.add(groupNamespace);
        }

        scriptDump.addGlobal("events", codes.toArray(Code[]::new));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        val classes = new HashSet<Class<?>>();

        for (val group : EventGroup.getGroups().values()) {
            for (val handler : group.getHandlers().values()) {
                if (!handler.scriptTypePredicate.test(scriptDump.scriptType)) {
                    continue;
                }
                classes.add(handler.eventType.get());
            }
        }

        return classes;
    }

    private static FunctionDeclaration formatEvent(TypeConverter converter, EventHandler handler, boolean useExtra) {
        val builder = Statements.func(handler.name);
        if (useExtra) {
            val typeDesc = handler.extra.describeType.apply(TypeDescAdapter.PROBEJS);
            val extraType = TypeDescAdapter.convertType(typeDesc);
            builder.param("extra", extraType);
        }
        val callback = Types.lambda()
            .param("event", Types.typeMaybeGeneric(handler.eventType.get()))
            .build();
        builder.param("handler", callback);
        return builder.build();
    }

    private static BiPredicate<EventGroup, EventHandler> getDisabledEvents(ScriptDump dump) {
        val filter = new EventJSFilter(dump);
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.disableEventDumps(filter));
        return filter.freeze();
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        for (val group : EventGroup.getGroups().keySet()) {
            filter.denyConstant(group);
        }
    }
}

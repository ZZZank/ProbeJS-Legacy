package zzzank.probejs.docs.events;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.features.kubejs.EventJSInfo;
import zzzank.probejs.features.kubejs.EventJSInfos;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.ts.FunctionDeclaration;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.*;
import java.util.stream.Collectors;

public class KubeEvents implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val disabled = getSkippedEvents(scriptDump);
        val scriptType = scriptDump.scriptType;
        val converter = scriptDump.transpiler.typeConverter;

        val events = new TreeMap<String, EventJSInfo>();
        synchronized (EventJSInfos.KNOWN) {
            for (var entry : EventJSInfos.KNOWN.entrySet()) {
                val id = entry.getKey();
                val info = entry.getValue();
                if (!disabled.contains(id) && info.scriptTypes.contains(scriptType)) {
                    events.put(id, entry.getValue());
                }
            }
        }

        List<Code> codes = new ArrayList<>();
        for (val entry : events.entrySet()) {
            val info = entry.getValue();
            val id = entry.getKey();

            val decl = declareEventMethod(Types.literal(id), converter, info);

            decl.addComment(
                "@at " + info.scriptTypes.stream().map(Objects::toString).collect(Collectors.joining(", ")));
            if (info.cancellable == null) {
                decl.addComment("@cancellable Unknown, this event data is gathered from ProbeJS predefined data");
            } else if (info.cancellable) {
                decl.addComment("@cancellable Yes");
            } else {
                decl.addComment("@cancellalle No");
            }

            if (!info.sub.isEmpty()) {
                decl.addComment(String.format(
                    "This event provides sub-event variant, e.g. `%s.%s`",
                    id,
                    info.sub
                ));
                codes.add(declareEventMethod(Types.templateLiteral(id + ".${string}"), converter, info));
            }
            codes.add(decl);
        }

        scriptDump.addGlobal("events", codes.toArray(new Code[0]));
    }

    private static @NotNull FunctionDeclaration declareEventMethod(
        BaseType id,
        TypeConverter converter,
        EventJSInfo info
    ) {
        return Statements
            .func("onEvent")
            .param("id", id)
            .param(
                "handler", Types.lambda()
                    .param("event", converter.convertType(info.clazz))
                    .build()
            )
            .build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return EventJSInfos.provideClasses();
    }

    private static Set<String> getSkippedEvents(ScriptDump dump) {
        val events = new HashSet<String>();
        ProbeJSPlugins.forEachPlugin(plugin -> events.addAll(plugin.disableEventDumps(dump)));
        return events;
    }

    @Override
    public void denyBindings(BindingFilter filter) {
        filter.denyFunction("onEvent");
    }
}

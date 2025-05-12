package zzzank.probejs.docs.bindings;

import dev.latvian.mods.rhino.NativeJavaClass;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.features.kubejs.BindingFilter;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.ts.ReexportDeclaration;
import zzzank.probejs.lang.typescript.code.ts.VariableDeclaration;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Bindings implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        val reader = new BindingReader(scriptDump);
        reader.read();

        val filter = new BindingFilter();
        ProbeJSPlugins.forEachPlugin(plugin -> plugin.denyBindings(filter));

        val converter = scriptDump.transpiler.typeConverter;

        val exported = new TreeMap<String, BaseType>();
        val reexported = new TreeMap<String, BaseType>(); // Namespaces

        for (val entry : reader.functions.entrySet()) {
            val name = entry.getKey();
            if (filter.isFunctionDenied(name)) {
                continue;
            }

            val fn = Types.lambda()
                .returnType(Types.ANY)
                .param("args", Types.ANY.asArray(), false, true)
                .build();
            exported.put(name, fn);
        }

        for (val entry : reader.constants.entrySet()) {
            val name = entry.getKey();
            if (filter.isConstantDenied(name)) {
                continue;
            }
            val obj = entry.getValue();
            exported.put(name, converter.convertType(obj.getClass()));
        }

        for (val entry : reader.classes.entrySet()) {
            val id = entry.getKey();
            if (filter.isClassDenied(id)) {
                continue;
            }
            val c = entry.getValue();
            if (c.isInterface()) {
                reexported.put(id, converter.convertType(c));
            } else {
                exported.put(id, Types.typeOf(converter.convertType(c)));
            }
        }

        if (ProbeConfig.resolveGlobal.get()) {
            ResolveGlobal.addGlobals(scriptDump);
            val oldGlobal = exported.get(ResolveGlobal.NAME);
            if (oldGlobal != null) {
                exported.put(ResolveGlobal.NAME, oldGlobal.and(ResolveGlobal.RESOLVED));
            } else {
                ProbeJS.LOGGER.error("no 'global' found in bindings, WHAT?");
            }
        }

        val codes = new ArrayList<Code>();
        for (val entry : exported.entrySet()) {
            val symbol = entry.getKey();
            val type = entry.getValue();
            codes.add(new VariableDeclaration(symbol, type));
        }
        for (val entry : reexported.entrySet()) {
            val symbol = entry.getKey();
            val type = entry.getValue();
            codes.add(new ReexportDeclaration(symbol, type));
        }
        scriptDump.addGlobal("bindings", exported.keySet(), codes.toArray(new Code[0]));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        val reader = new BindingReader(scriptDump);
        reader.read();

        Set<Class<?>> classes = new HashSet<>(reader.classes.values());
        for (val o : reader.constants.values()) {
            if (o instanceof NativeJavaClass njc) {
                classes.add(njc.getClassObject());
            } else if (o instanceof Class<?> c) {
                classes.add(c);
            } else {
                classes.add(o.getClass());
            }
        }

        return classes;
    }
}

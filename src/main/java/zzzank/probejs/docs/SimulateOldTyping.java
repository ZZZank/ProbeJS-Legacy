package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.CollectUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class SimulateOldTyping implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (!ProbeConfig.simulateOldTyping.get()) {
            return;
        }
        val typeConverter = scriptDump.transpiler.typeConverter;

        val namespace = new Wrapped.Namespace("Internal");

        val recorded = new HashSet<String>(scriptDump.recordedClasses.size());
        for (val clazz : scriptDump.recordedClasses) {
            val name = clazz.classPath.getJavaName();

            if (clazz.variableTypes.isEmpty()) {
                namespace.addCode(new TypeDecl(
                    getUniqueName(name, recorded),
                    Types.type(clazz.classPath).contextShield(BaseType.FormatType.RETURN)
                ));
                namespace.addCode(new TypeDecl(getUniqueName(name + "_", recorded), Types.type(clazz.classPath)));
            } else {
                val variables = CollectUtils.mapToList(clazz.variableTypes, typeConverter::convertType);
                val variableType = Types.type(clazz.classPath).withParams(variables);
                namespace.addCode(Statements
                    .type(getUniqueName(name, recorded), variableType)
                    .symbolVariables(variables)
                    .typeFormat(BaseType.FormatType.RETURN)
                    .build());
                namespace.addCode(Statements
                    .type(getUniqueName(name + "_", recorded), variableType)
                    .symbolVariables(variables)
                    .typeFormat(BaseType.FormatType.INPUT)
                    .build());
            }
        }

        scriptDump.addGlobal("simulated_internal", namespace);
    }

    private static String getUniqueName(String name, Set<String> recorded) {
        var counter = 0;
        while (recorded.contains(name)) {
            name = name + counter;
            counter++;
        }
        recorded.add(name);
        return name;
    }
}

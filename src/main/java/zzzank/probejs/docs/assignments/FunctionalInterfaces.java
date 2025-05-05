package zzzank.probejs.docs.assignments;

import lombok.val;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

public class FunctionalInterfaces implements ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        val converter = scriptDump.transpiler().typeConverter;

        for (val recorded : scriptDump.recordedClasses) {
            if (!recorded.isInterface()) {
                continue;
            }

            MethodInfo abstractM = null;
            for (val method : recorded.methods) {
                if (!method.attributes.isAbstract) {
                    continue;
                }
                if (abstractM == null) {
                    abstractM = method;
                } else {
                    abstractM = null;
                    break;
                }
            }
            if (abstractM == null) {
                continue;
            }

            val type = Types.lambda().returnType(converter.convertType(abstractM.returnType));
            for (val param : abstractM.params) {
                type.param(param.name, converter.convertType(param.type), false, param.varArgs);
            }
            scriptDump.assignType(recorded.getOriginal(), type.build());
        }
    }
}

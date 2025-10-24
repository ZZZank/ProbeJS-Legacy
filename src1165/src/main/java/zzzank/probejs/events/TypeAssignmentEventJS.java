package zzzank.probejs.events;

import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.BaseType;

public class TypeAssignmentEventJS extends ScriptEventJS {

    public TypeAssignmentEventJS(ScriptDump scriptDump) {
        super(scriptDump);
    }

    public void assignType(Class<?> clazz, BaseType baseType) {
        scriptDump.assignType(clazz, baseType);
    }
}

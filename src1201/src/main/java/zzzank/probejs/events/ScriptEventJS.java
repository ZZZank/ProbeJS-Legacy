package zzzank.probejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.utility.TSUtilityType;
import zzzank.probejs.utils.ClassWrapperPJS;

public class ScriptEventJS extends EventJS {
    public final ScriptDump scriptDump;

    public ScriptEventJS(ScriptDump scriptDump) {
        this.scriptDump = scriptDump;
    }

    public ScriptType getScriptType() {
        return scriptDump.scriptType;
    }

    public ClassWrapperPJS<Types> getTypes() {
        return new ClassWrapperPJS<>(Types.class);
    }

    public ClassWrapperPJS<Statements> getStatements() {
        return new ClassWrapperPJS<>(Statements.class);
    }

    public ClassWrapperPJS<TSUtilityType> getTSUtilityTypes() {
        return new ClassWrapperPJS<>(TSUtilityType.class);
    }
}

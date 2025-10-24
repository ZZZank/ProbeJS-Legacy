package zzzank.probejs.events;

import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.Code;

import java.util.Arrays;

public class AddGlobalEventJS extends ScriptEventJS {

    public AddGlobalEventJS(ScriptDump scriptDump) {
        super(scriptDump);
    }

    public void addGlobal(String identifier, Code... content) {
        scriptDump.addGlobal(identifier, content);
    }

    public void addGlobal(String identifier, String[] excludedNames, Code... content) {
        scriptDump.addGlobal(identifier, Arrays.asList(excludedNames), content);
    }
}

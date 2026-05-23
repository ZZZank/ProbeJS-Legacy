package zzzank.probejs.plugin;

import dev.latvian.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.*;
import org.jetbrains.annotations.Nullable;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.remap.RemapperBridge;

public class Require extends BaseFunction {
    private final ScriptManager manager;

    public Require(ScriptManager manager) {
        this.manager = manager;
    }

    /// ```javascript
    /// const { $ArrayList, $HashMap } = require("java/util");
    /// ```
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable self, Object[] args) {
        if (!ProbeConfig.isolatedScopes.get()) {
            manager.type.console.error(String.format(
                "'require(...)' used without enabling '%s' config, this might cause inconsistency between IDE report and actual behaviour",
                ProbeConfig.isolatedScopes.name()
            ));
        }

        String path;
        if (args.length == 0) {
            return new RequireWrapper(manager, null);
        } else {
            path = args[0].toString();
            if (!path.startsWith("java:")) {
                return new RequireWrapper(manager, null);
            }
        }

        return new RequireWrapper(manager, path.substring("java:".length()).replace('/', '.'));
    }

    public static class RequireWrapper extends ScriptableObject {
        private final ScriptManager manager;
        @Nullable
        private final String prefix;

        private RequireWrapper(ScriptManager manager, @Nullable String prefix) {
            this.manager = manager;
            this.prefix = prefix;
        }

        @Override
        public String getClassName() {
            return "ProbeJSRequire";
        }

        @Override
        public Object get(String name, Scriptable start) {
            if (prefix == null) {
                return Undefined.instance;
            }
            if (name.startsWith("$")) {
                var args = new String[]{RemapperBridge.unmapClass(prefix + '.' + name.substring(1))};
                return manager.loadJavaClass(start, args);
            }
            return super.get(name, start);
        }
    }
}

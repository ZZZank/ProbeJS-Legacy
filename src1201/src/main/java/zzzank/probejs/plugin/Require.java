package zzzank.probejs.plugin;

import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.util.HideFromJS;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;

public class Require extends BaseFunction {
    private final ScriptManager manager;

    public Require(ScriptManager manager) {
        this.manager = manager;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable self, Object[] args) {
        if (!ProbeConfig.isolatedScopes.get()) {
            manager.scriptType.console.warn(String.format(
                "'require(...)' used without enabling '%s' config, this might cause inconsistency between IDE report and actual behaviour",
                ProbeConfig.isolatedScopes.name()
            ));
        }
        String name;
        if (args.length == 0 || (name = args[0].toString()).startsWith("./")) {
            return new RequireWrapper(null, Undefined.instance);
        }

        try {
            var loaded = manager.loadJavaClass(name, true);
            var path = ClassPath.ofJava(loaded.getClassObject());
            return new RequireWrapper(path, loaded);
        } catch (Exception ignored) {
            manager.scriptType.console.error(String.format("Class '%s' not found, returning undefined value", name));
            return new RequireWrapper(null, Undefined.instance);
        }
    }

    public static class RequireWrapper extends ScriptableObject {
        private final ClassPath path;
        private final Object clazz;

        public RequireWrapper(ClassPath path, Object clazz) {
            assert clazz == Undefined.instance || clazz instanceof NativeJavaClass;
            this.path = path;
            this.clazz = clazz;
        }

        @Override
        public String getClassName() {
            return path.getRemappedName();
        }

        @Override
        public Object get(Context cx, String name, Scriptable start) {
            if (path == null || name.equals(path.getSimpleName())) {
                return clazz;
            }
            return super.get(cx, name, start);
        }

        @HideFromJS
        public ClassPath getPath() {
            return path;
        }
    }
}

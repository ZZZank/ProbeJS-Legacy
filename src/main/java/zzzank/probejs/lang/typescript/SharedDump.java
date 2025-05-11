package zzzank.probejs.lang.typescript;

import zzzank.probejs.api.dump.TSFilesDump;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class SharedDump extends TSFilesDump {
    public final Transpiler transpiler = new Transpiler();
    public final Set<ClassPath> denied = new HashSet<>();

    public SharedDump(Path writeTo) {
        super(writeTo);
        this.modifiers.add(f -> !denied.contains(f.path));
        transpiler.init();
    }

    @Override
    protected void dumpImpl() throws IOException {
        this.files = transpiler.dump(ClassRegistry.REGISTRY.getFoundClasses()).values();
        super.dumpImpl();
    }
}
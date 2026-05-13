package zzzank.probejs.lang.typescript;

import zzzank.probejs.ProbeConfig;
import zzzank.probejs.api.dump.TSFilesDump;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.util.BuiltinDocHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class SharedDump extends TSFilesDump implements ProbeNamedDump {
    public final Transpiler transpiler;
    public final Set<ClassPath> denied = new HashSet<>();

    public SharedDump(Path writeTo, Transpiler transpiler) {
        super(writeTo);
        this.transpiler = transpiler;
    }

    @Override
    public String pjsDumpName() {
        return "SHARED";
    }

    @Override
    public void open() throws IOException {
        this.files = transpiler.dump(ClassRegistry.REGISTRY.getFoundClasses())
            .values()
            .stream()
            .filter(f -> !denied.contains(f.path))
            .toList();
        for (var file : files) {
            BuiltinDocHelper.injectConvertibleTypeDecl(file, Collections.emptyList());

            boolean convertField = ProbeConfig.fieldAsBeaning.get();
            file.findCode(ClassDecl.class).ifPresent(classdecl -> BuiltinDocHelper.injectBeaning(classdecl, convertField));
        }
        super.open();
    }
}
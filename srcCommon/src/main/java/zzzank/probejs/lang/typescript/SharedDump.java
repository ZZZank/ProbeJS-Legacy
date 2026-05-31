package zzzank.probejs.lang.typescript;

import zzzank.probejs.ProbeConfig;
import zzzank.probejs.api.dump.TSFilesDump;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;
import zzzank.probejs.util.BuiltinDocHelper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * @author ZZZank
 */
public class SharedDump extends TSFilesDump implements ProbeNamedDump {
    public final ClassRegistry classRegistry;
    public final Transpiler transpiler;
    public final Set<ClassPath> denied = new HashSet<>();

    public SharedDump(Path writeTo, ClassRegistry classRegistry, Transpiler transpiler) {
        super(writeTo);
        this.classRegistry = classRegistry;
        this.transpiler = transpiler;
    }

    @Override
    public String pjsDumpName() {
        return "SHARED";
    }

    @Override
    public void open() throws IOException {
        this.files = transpiler.dump(classRegistry.getFoundClasses())
            .values()
            .stream()
            .filter(f -> !denied.contains(f.path))
            .toList();
        for (var file : files) {
            BuiltinDocHelper.injectConvertibleTypeDecl(file, Collections.emptyList());

            boolean convertField = ProbeConfig.fieldAsBeaning.get();
            if (file instanceof NativeClassFile nativeFile) {
                var classDecl = nativeFile.nativeClass;
                if (classDecl instanceof InterfaceDecl interfaceDecl) {
                    classDecl = interfaceDecl.getStaticClass();
                }
                BuiltinDocHelper.injectBeaning(classDecl, convertField);
            }
        }
        super.open();
    }
}
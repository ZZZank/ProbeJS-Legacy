package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Map;

/**
 * @author ZZZank
 */
public class KubeJSDenied implements ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        val scriptManager = scriptDump.manager;
        for (var scriptFile : globalClasses.values()) {
            val classDecl = scriptFile.findCodeNullable(ClassDecl.class);
            if (classDecl == null
                || classDecl.nativeClazz == null
                || scriptManager.isClassAllowed(classDecl.nativeClazz.getOriginal().getName())) {
                continue;
            }
            classDecl.linebreak();
            classDecl.addComment(
                "This class is not allowed By KubeJS!",
                "You should not load the class, or KubeJS will throw an error.",
                "Loading the class using require() will not throw an error, but the class will be undefined."
            );
        }
    }
}

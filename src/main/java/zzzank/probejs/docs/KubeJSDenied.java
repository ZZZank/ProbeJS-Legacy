package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.typescript.TypeSpecificFiles;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.plugin.ProbeJSPlugin;

/**
 * @author ZZZank
 */
public class KubeJSDenied implements ProbeJSPlugin {

    @Override
    public void modifyFiles(TypeSpecificFiles files) {
        val scriptManager = files.scriptDump().manager;
        for (val scriptFile : files.globalFiles().values()) {
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
            files.markRequested(scriptFile.path);
        }
    }
}

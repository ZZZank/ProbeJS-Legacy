package zzzank.probejs.docs;

import dev.latvian.kubejs.script.ScriptType;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.util.BuiltinDocHelper;

/**
 * @author ZZZank
 */
public class InjectBeaning implements ProbeJSPlugin {

    /// apply after all modification to method/field has been applied
    @Override
    public byte priority() {
        return -100;
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        boolean convertFields = ProbeConfig.fieldAsBeaning.get();

        for (var file : files.globalFiles().values()) {
            if (file.path.isArtificial()) {
                continue;
            }
            var classDecl = file.findCodeNullable(ClassDecl.class);
            if (classDecl != null) {
                BuiltinDocHelper.injectBeaning(classDecl, convertFields);
            }
        }
    }
}

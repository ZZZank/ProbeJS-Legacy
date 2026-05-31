package zzzank.probejs.docs;

import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.typescript.NativeClassFile;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;
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
            if (file instanceof NativeClassFile nativeFile
                && nativeFile.nativeClass instanceof InterfaceDecl interfaceDecl) {
                BuiltinDocHelper.injectBeaning(interfaceDecl.getStaticClass(), convertFields);
            }
        }
    }
}

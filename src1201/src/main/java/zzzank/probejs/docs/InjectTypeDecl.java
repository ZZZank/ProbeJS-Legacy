package zzzank.probejs.docs;

import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;
import zzzank.probejs.util.BuiltinDocHelper;

/**
 * @author ZZZank
 */
public class InjectTypeDecl implements ProbeJSPlugin {

    @Override
    public byte priority() {
        return 100;
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        var scriptDump = files.scriptDump();
        ProbeJSPlugins.forEachPlugin(p -> p.assignType(scriptDump));

        for (var entry : scriptDump.convertibles.asMap().entrySet()) {
            var path = entry.getKey();
            var convertibles = entry.getValue();
            var file = files.request(path);
            if (file != null) {
                BuiltinDocHelper.injectConvertibleTypeDecl(file, convertibles);
            }
        }
    }
}

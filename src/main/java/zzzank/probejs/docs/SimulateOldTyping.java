package zzzank.probejs.docs;

import lombok.val;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.lang.typescript.refer.ImportInfos;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.CollectUtils;

import java.util.List;

/**
 * @author ZZZank
 */
public class SimulateOldTyping implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        scriptDump.addGlobal("simulated_internal", new DocImpl(scriptDump));
    }

    public static class DocImpl extends Code {
        private final ScriptDump scriptDump;

        public DocImpl(ScriptDump scriptDump) {
            this.scriptDump = scriptDump;
        }

        @Override
        public ImportInfos getImportInfos() {
            val transpiler = scriptDump.transpiler;
            return ImportInfos.of(
                scriptDump.recordedClasses.stream()
                    .filter(c -> !transpiler.isRejected(c))
                    .map(c -> c.classPath)
                    .map(ImportInfo::ofDefault));
        }

        @Override
        public List<String> format(Declaration declaration) {
            val transpiler = scriptDump.transpiler;
            val namespace = new Wrapped.Namespace("Internal");

            for (val reference : declaration.references.values()) {
                val name = reference.deduped;
                if (!name.startsWith("$")) {
                    continue;
                }

                val path = reference.info.path;
                val clazz = path.toClazz(ClassRegistry.REGISTRY);
                if (clazz == null || transpiler.isRejected(clazz)) {
                    continue;
                }

                val typeName = name.substring(1);
                if (clazz.variableTypes.isEmpty()) {
                    val type = Types.type(path);
                    namespace.addCode(new TypeDecl(typeName, type).setTypeFormat(BaseType.FormatType.RETURN));
                    namespace.addCode(new TypeDecl(typeName + "_", type));
                } else {
                    val variables = CollectUtils.mapToList(
                        clazz.variableTypes,
                        transpiler.typeConverter::convertType
                    );
                    val type = Types.type(path).withParams(variables);
                    namespace.addCode(new TypeDecl(typeName, variables, type).setTypeFormat(BaseType.FormatType.RETURN));
                    namespace.addCode(new TypeDecl(typeName + "_", variables, type));
                }
            }
            return namespace.format(declaration);
        }
    }
}

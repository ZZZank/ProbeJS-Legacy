package zzzank.probejs.docs;

import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.Declaration;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.ts.Wrapped;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.CollectUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author ZZZank
 */
public class SimulateOldTyping implements ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (!ProbeConfig.simulateOldTyping.get()) {
            return;
        }

        val transpiler = scriptDump.transpiler;
        val namespace = new Wrapped.Namespace("Internal");

        for (val clazz : scriptDump.recordedClasses) {
            val path = clazz.classPath;
            if (transpiler.isRejected(clazz)) {
                continue;
            }

            if (clazz.variableTypes.isEmpty()) {
                namespace.addCode(new NameInferredTypeDecl(path).setTypeFormat(BaseType.FormatType.RETURN));
                namespace.addCode(new NameInferredTypeDecl(path));
            } else {
                val variables = CollectUtils.mapToList(
                    clazz.variableTypes,
                    transpiler.typeConverter::convertVariableType
                );
                namespace.addCode(new NameInferredTypeDecl(variables, path).setTypeFormat(BaseType.FormatType.RETURN));
                namespace.addCode(new NameInferredTypeDecl(variables, path));
            }
        }

        scriptDump.addGlobal("simulated_internal", namespace);
    }

    public static class NameInferredTypeDecl extends TypeDecl {
        private final ClassPath path;

        public NameInferredTypeDecl(@NotNull ClassPath path) {
            super("", Types.type(path));
            this.path = path;
        }

        public NameInferredTypeDecl(@NotNull List<TSVariableType> symbolVariables, @NotNull ClassPath path) {
            super("", symbolVariables, Types.type(path).withParams(symbolVariables));
            this.path = path;
        }

        @Override
        public List<String> formatRaw(Declaration declaration) {
            val reference = declaration.references.get(path);
            var name = reference.deduped;
            if (!name.startsWith("$")) {
                return Collections.emptyList();
            }
            name = name.substring(1);
            if (this.typeFormat == BaseType.FormatType.INPUT) {
                name = name + '_';
            }
            this.name = name;

            return super.formatRaw(declaration);
        }
    }
}

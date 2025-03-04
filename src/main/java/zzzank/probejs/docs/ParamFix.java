package zzzank.probejs.docs;

import dev.latvian.kubejs.bindings.TextWrapper;
import dev.latvian.kubejs.recipe.RecipeEventJS;
import dev.latvian.kubejs.text.Text;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.minecraft.core.Registry;
import zzzank.probejs.docs.assignments.SpecialTypes;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.DocUtils;
import zzzank.probejs.utils.NameUtils;

import java.util.Map;

public class ParamFix implements ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        val helper = new Helper(scriptDump, globalClasses);
        if (helper.refreshTSFile(TextWrapper.class) != null) {
            DocUtils.replaceParamType(
                helper.file,
                m -> m.params.size() == 1 && m.name.equals("of"),
                0,
                Types.type(Text.class)
            );
        }
        if (helper.refreshTSFile(RecipeEventJS.class) != null) {
            DocUtils.replaceParamType(
                helper.file,
                m -> m.params.size() == 1 && m.name.equals("custom"),
                0,
                Types.object()
                    .member(
                        "type",
                        Types.primitive(
                            SpecialTypes.dot(
                                NameUtils.registryName(Registry.RECIPE_SERIALIZER_REGISTRY)
                            )
                        )
                    )
                    .literalMember("[x: string]", Types.ANY)
                    .build()
                    .comment("other recipe json elements are unknown to ProbeJS :(")
            );
        }
    }

    @RequiredArgsConstructor
    static class Helper {
        final ScriptDump scriptDump;
        final Map<ClassPath, TypeScriptFile> globalClasses;
        TypeScriptFile file;

        TypeScriptFile refreshTSFile(ClassPath path) {
            return file = globalClasses.get(path);
        }

        TypeScriptFile refreshTSFile(Class<?> type) {
            return refreshTSFile(ClassPath.fromJava(type));
        }
    }
}

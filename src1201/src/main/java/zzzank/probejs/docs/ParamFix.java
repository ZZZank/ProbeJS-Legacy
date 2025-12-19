package zzzank.probejs.docs;

import dev.latvian.mods.kubejs.bindings.TextWrapper;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import zzzank.probejs.docs.assignments.SpecialTypes;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.TypeScriptFile;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.utils.DocUtils;
import zzzank.probejs.utils.GameUtils;

public class ParamFix implements ProbeJSPlugin {
    @Override
    public void modifyFiles(RequestAwareFiles files) {
        TypeScriptFile file;

        file = files.request(TextWrapper.class);
        if (file != null) {
            DocUtils.replaceParamType(
                file,
                m -> m.params.size() == 1 && m.name.equals("of"),
                0,
                Types.type(Component.class)
            );
        }

        file = files.request(RecipesEventJS.class);
        if (file != null) {
            DocUtils.replaceParamType(
                file,
                m -> m.params.size() == 1 && m.name.equals("custom"),
                0,
                Types.object()
                    .member(
                        "type",
                        Types.primitive(
                            SpecialTypes.dot(
                                GameUtils.registryName(Registries.RECIPE_SERIALIZER)
                            )
                        )
                    )
                    .rawNameMember("[x: string]", Types.ANY)
                    .build()
                    .comment("other recipe json elements are unknown to ProbeJS :(")
            );
        }
    }
}

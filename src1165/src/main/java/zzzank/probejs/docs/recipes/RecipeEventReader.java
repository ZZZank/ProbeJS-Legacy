package zzzank.probejs.docs.recipes;

import dev.latvian.kubejs.recipe.RecipeFunction;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.lang.typescript.code.type.js.JSObjectType;

import java.util.Map;

/**
 * @author ZZZank
 */
public final class RecipeEventReader {
    public final JSObjectType.Builder result = Types.object();
    private final TypeConverter converter;
    private final Map<ResourceLocation, JSLambdaType> predefined;

    public RecipeEventReader(TypeConverter converter, Map<ResourceLocation, JSLambdaType> predefined) {
        this.converter = converter;
        this.predefined = predefined;
    }

    public void read(Map<String, Object> recipesMap) {
        readImpl(result, recipesMap);
    }

    private void readImpl(JSObjectType.Builder builder, Map<?, ?> recipes) {
        for (val entry : recipes.entrySet()) {
            val key = entry.getKey().toString();
            val value = entry.getValue();
            if (value instanceof RecipeFunction rFn) {
                var recipeFn = predefined.get(rFn.typeID);
                val type = rFn.type;
                if (recipeFn == null) {
                    if (!type.isCustom()) {
                        recipeFn = Types.lambda()
                            .param("args", Types.ANY, false, true)
                            .returnType(converter.convertType(rFn.type.factory.get().getClass()))
                            .build();
                    } else if (ProbeConfig.dumpCustomRecipeGenerator.get()) {
                        recipeFn = Types.lambda()
                            .param("recipeJson", Types.EMPTY_OBJECT)
                            .returnType(converter.convertType(rFn.type.factory.get().getClass()))
                            .build();
                    }
                }
                if (recipeFn != null) {
                    builder.member(key, recipeFn);
                }
            } else if (value instanceof Map<?, ?> m) {
                val sub = Types.object();
                readImpl(sub, m);
                builder.member(key, sub.build());
            } else {
                ProbeJS.LOGGER.warn(
                    "found unknown object in RecipeEvents#getRecipes(): '{}', type: '{}'",
                    value,
                    value == null ? null : value.getClass()
                );
            }
        }
    }
}

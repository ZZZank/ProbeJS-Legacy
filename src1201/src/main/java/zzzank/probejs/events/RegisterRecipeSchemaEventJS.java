package zzzank.probejs.events;

import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import net.minecraft.resources.ResourceLocation;

/**
 * @author ZZZank
 */
public class RegisterRecipeSchemaEventJS extends RecipeSchemaRelatedEventBase {
    public final RegisterRecipeSchemasEvent rawEvent;

    public RegisterRecipeSchemaEventJS(RegisterRecipeSchemasEvent rawEvent) {
        this.rawEvent = rawEvent;
    }

    public RecipeNamespace namespace(String namespace) {
        return rawEvent.namespace(namespace);
    }

    public void register(ResourceLocation id, RecipeSchema schema) {
        rawEvent.register(id, schema);
    }

    public void mapRecipe(String name, ResourceLocation type) {
        rawEvent.mapRecipe(name, type);
    }

    public void mapRecipe(String name, String type) {
        rawEvent.mapRecipe(name, type);
    }
}

package zzzank.probejs.events;

import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.DynamicRecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeComponentFactoryRegistryEvent;

/**
 * @author ZZZank
 */
public class RegisterRecipeComponentFactoryEventJS extends RecipeSchemaRelatedEventBase {
    public final RecipeComponentFactoryRegistryEvent rawEvent;

    public RegisterRecipeComponentFactoryEventJS(RecipeComponentFactoryRegistryEvent rawEvent) {
        this.rawEvent = rawEvent;
    }

    public void register(String name, RecipeComponent<?> component) {
        rawEvent.register(name, component);
    }

    public void registerDynamic(String name, DynamicRecipeComponent component) {
        rawEvent.registerDynamic(name, component);
    }
}

package zzzank.probejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.*;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import zzzank.probejs.utils.ClassWrapperPJS;

/**
 * @author ZZZank
 */
public class RecipeSchemaRelatedEventBase extends EventJS {

    // class reference

    public ClassWrapperPJS<RecipeSchema> get$RecipeSchema() {
        return new ClassWrapperPJS<>(RecipeSchema.class);
    }

    public ClassWrapperPJS<RecipeComponent> get$RecipeComponent() {
        return new ClassWrapperPJS<>(RecipeComponent.class);
    }

    public ClassWrapperPJS<FluidComponents> get$FluidComponents() {
        return new ClassWrapperPJS<>(FluidComponents.class);
    }

    public ClassWrapperPJS<ItemComponents> get$ItemComponents() {
        return new ClassWrapperPJS<>(ItemComponents.class);
    }

    public ClassWrapperPJS<NumberComponent> get$NumberComponent() {
        return new ClassWrapperPJS<>(NumberComponent.class);
    }

    public ClassWrapperPJS<EnumComponent> get$EnumComponent() {
        return new ClassWrapperPJS<>(EnumComponent.class);
    }

    public ClassWrapperPJS<StringComponent> get$StringComponent() {
        return new ClassWrapperPJS<>(StringComponent.class);
    }

    public ClassWrapperPJS<TagKeyComponent> get$TagKeyComponent() {
        return new ClassWrapperPJS<>(TagKeyComponent.class);
    }

    public ClassWrapperPJS<TimeComponent> get$TimeComponent() {
        return new ClassWrapperPJS<>(TimeComponent.class);
    }

    // helper method

    public RecipeSchema createRecipeSchema(RecipeKey<?>... keys) {
        return new RecipeSchema(keys);
    }

    public <T> RecipeKey<T> createRecipeKey(String name, RecipeComponent<T> component) {
        return new RecipeKey<>(component, name);
    }
}

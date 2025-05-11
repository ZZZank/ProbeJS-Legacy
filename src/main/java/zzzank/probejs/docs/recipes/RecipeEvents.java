package zzzank.probejs.docs.recipes;

import dev.latvian.kubejs.recipe.RecipeEventJS;
import dev.latvian.kubejs.recipe.RecipeFunction;
import dev.latvian.kubejs.script.ScriptType;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.TypeSpecificFiles;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.member.TypeDecl;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.*;

public class RecipeEvents implements ProbeJSPlugin {

    private static Map<String, Object> capturedRecipes;

    public static void captureRecipes(Map<String, Object> recipes) {
        capturedRecipes = new HashMap<>(recipes);
    }

    public static final Map<String, ResourceLocation> SHORTCUTS = new HashMap<>();
    public static final JSPrimitiveType DOCUMENTED = Types.primitive("DocumentedRecipes");

    static {
        SHORTCUTS.put("shaped", new ResourceLocation("kubejs", "shaped"));
        SHORTCUTS.put("shapeless", new ResourceLocation("kubejs", "shapeless"));
        SHORTCUTS.put("smelting", new ResourceLocation("minecraft", "smelting"));
        SHORTCUTS.put("blasting", new ResourceLocation("minecraft", "blasting"));
        SHORTCUTS.put("smoking", new ResourceLocation("minecraft", "smoking"));
        SHORTCUTS.put("campfireCooking", new ResourceLocation("minecraft", "campfire_cooking"));
        SHORTCUTS.put("stonecutting", new ResourceLocation("minecraft", "stonecutting"));
        SHORTCUTS.put("smithing", new ResourceLocation("minecraft", "smithing"));
    }

    private Map<ResourceLocation, JSLambdaType> getPredefinedRecipeDocs(ScriptDump scriptDump) {
        val pred = new HashMap<ResourceLocation, JSLambdaType>();
        ProbeJSPlugins.forEachPlugin(p -> p.addPredefinedRecipeDoc(scriptDump, pred));
        ProbeJS.LOGGER.debug("Read {} predefined recipe docs", pred.size());
        return pred;
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType == ScriptType.CLIENT) {
            return;
        }

        val reader = new RecipeEventReader(scriptDump.transpiler.typeConverter, getPredefinedRecipeDocs(scriptDump));
        reader.read(capturedRecipes);
        val parsed = reader.result.build().contextShield(BaseType.FormatType.RETURN);

        scriptDump.addGlobal("resolved_recipes", new TypeDecl(DOCUMENTED.content, parsed));
    }

    @Override
    public void modifyFiles(TypeSpecificFiles files) {
        if (files.scriptDump().scriptType == ScriptType.CLIENT) {
            return;
        }

        //2.Inject types into the RecipeEventJS
        val recipeEvent = files.requestCode(RecipeEventJS.class, ClassDecl.class);
        if (recipeEvent == null) {
            ProbeJS.LOGGER.error("RecipeEventJS class declaration not found");
            return; // What???
        }

        for (val m : recipeEvent.methods) {
            if (m.params.isEmpty() && m.name.equals("getRecipes")) {
                m.returnType = DOCUMENTED;
                break;
            }
        }
        for (val code : recipeEvent.bodyCode) {
            if (code instanceof BeanDecl.Getter beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.type = DOCUMENTED;
                break;
            }
        }

        //3.Make shortcuts valid recipe functions
        for (val field : recipeEvent.fields) {
            val parts = SHORTCUTS.get(field.name);
            if (parts == null) {
                continue;
            }

            field.type = Types.primitive(
                String.format(
                    "%s[%s][%s]",
                    DOCUMENTED.content,
                    ProbeJS.GSON.toJson(parts.getNamespace()),
                    ProbeJS.GSON.toJson(parts.getPath())
                )
            );
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        if (scriptDump.scriptType == ScriptType.CLIENT) {
            return Collections.emptySet();
        }

        val jClasses = collectClasses(capturedRecipes);
        //make sure RecipeEventJS has TSFile generated, to prevent modifyClasses from failing
        jClasses.add(RecipeEventJS.class);
        return jClasses;
    }

    private static Set<Class<?>> collectClasses(Map<?, ?> recipes) {
        val collected = new HashSet<Class<?>>();
        for (val value : recipes.values()) {
            if (value instanceof RecipeFunction rFn) {
                collected.add(rFn.type.getClass());
                collected.add(rFn.type.factory.get().getClass());
            } else if (value instanceof Map<?, ?> m) {
                collected.addAll(collectClasses(m));
            }
        }
        return collected;
    }
}

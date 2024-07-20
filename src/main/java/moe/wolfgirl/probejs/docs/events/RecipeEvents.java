package moe.wolfgirl.probejs.docs.events;

import dev.latvian.kubejs.recipe.RecipeEventJS;
import dev.latvian.kubejs.recipe.RecipeTypeJS;
import dev.latvian.kubejs.recipe.RegisterRecipeHandlersEvent;
import dev.latvian.kubejs.script.ScriptType;
import dev.latvian.kubejs.util.KubeJSPlugins;
import lombok.val;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectBeans;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class RecipeEvents extends ProbeJSPlugin {

    public static final Map<String, ResourceLocation> SHORTCUTS = new HashMap<>();
    public static final String PATH_BASE = "moe.wolfgirl.probejs.generated.recipes";
    public static final ClassPath DOCUMENTED = new ClassPath(PATH_BASE + ".DocumentedRecipes");

    static {
        SHORTCUTS.put("shaped", new ResourceLocation("kubejs", "shaped"));
        SHORTCUTS.put("shapeless", new ResourceLocation("kubejs", "shapeless"));
        SHORTCUTS.put("smelting", new ResourceLocation("minecraft", "smelting"));
        SHORTCUTS.put("blasting", new ResourceLocation("minecraft", "blasting"));
        SHORTCUTS.put("smoking", new ResourceLocation("minecraft", "smoking"));
        SHORTCUTS.put("campfireCooking", new ResourceLocation("minecraft", "campfire_cooking"));
        SHORTCUTS.put("stonecutting", new ResourceLocation("minecraft", "stonecutting"));
        SHORTCUTS.put("smithing", new ResourceLocation("minecraft", "smithing_transform"));
    }

    public final Map<ResourceLocation, RecipeTypeJS> ALL = new HashMap<>();

    public RecipeEvents() {
        val recipeEvent = new RegisterRecipeHandlersEvent(ALL);
        KubeJSPlugins.forEachPlugin(plugin -> plugin.addRecipes(recipeEvent));
    }

    private Map<String, Map<String, RecipeTypeJS>> getGroupedRecipeTypes() {
        val grouped = new HashMap<String, Map<String, RecipeTypeJS>>();
        for (Map.Entry<ResourceLocation, RecipeTypeJS> entry : ALL.entrySet()) {
            val namespace = entry.getKey().getNamespace();
            val name = entry.getKey().getPath();
            val type = entry.getValue();
            grouped
                .computeIfAbsent(namespace, k -> new HashMap<>())
                .put(name, type);
        }
        return grouped;
    }

    private Map<ResourceLocation, JSLambdaType> getPredefinedRecipeDocs(ScriptDump scriptDump) {
        val pred = new HashMap<ResourceLocation, JSLambdaType>();
        ProbeJSPlugin.forEachPlugin(p -> p.addPredefinedRecipeDoc(scriptDump, pred));
        return pred;
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }
        val converter = scriptDump.transpiler.typeConverter;

        // Generate recipe schema classes
        // Also generate the documented recipe class containing all stuffs from everywhere
        val documentedRecipes = Statements.clazz(DOCUMENTED.getName());

        val grouped = getGroupedRecipeTypes();
        val predefinedTypes = getPredefinedRecipeDocs(scriptDump);

        for (val entry : grouped.entrySet()) {
            val namespace = entry.getKey();
            val group = entry.getValue();

            val namespaced = Types.object();
            for (Map.Entry<String, RecipeTypeJS> e : group.entrySet()) {
                val path = e.getKey();

                var fn = predefinedTypes.get(new ResourceLocation(namespace, path));
                if (fn == null) {
                    val type = e.getValue();
                    fn = Types.lambda()
                        .param("args", Types.ANY, false, true)
                        .returnType(converter.convertType(type.getClass()))
                        .build();
                }

                namespaced.member(path, fn);
            }
            documentedRecipes.field(namespace, namespaced.build());
        }

        TypeScriptFile documentFile = new TypeScriptFile(DOCUMENTED);
        documentFile.addCode(documentedRecipes.build());
        globalClasses.put(DOCUMENTED, documentFile);

        // Inject types into the RecipeEventJS
        val recipeEventFile = globalClasses.get(new ClassPath(RecipeEventJS.class));
        val recipeEvent = recipeEventFile.findCode(ClassDecl.class).orElse(null);
        if (recipeEvent == null) {
            ProbeJS.LOGGER.error("RecipeEventJS class declaration not found");
            return; // What???
        }
        recipeEvent.methods.stream()
            .filter(m -> m.params.isEmpty() && m.name.equals("getRecipes"))
            .findFirst()
            .ifPresent(methodDecl -> methodDecl.returnType = Types.type(DOCUMENTED));
        for (val code : recipeEvent.bodyCode) {
            if (code instanceof InjectBeans.BeanDecl beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.baseType = Types.type(DOCUMENTED);
                break;
            }
        }
        recipeEventFile.declaration.addClass(DOCUMENTED);

        // Make shortcuts valid recipe functions
        for (val field : recipeEvent.fields) {
            if (!SHORTCUTS.containsKey(field.name)) {
                continue;
            }
            val parts = SHORTCUTS.get(field.name);
            if (predefinedTypes.containsKey(parts)) {
                field.type = predefinedTypes.get(parts);
            } else {
                val type = grouped.get(parts.getNamespace()).get(parts.getPath());
                field.type = Types.lambda()
                    .param("args", Types.ANY, false, true)
                    .returnType(converter.convertType(type.getClass()))
                    .build();
            }

            for (ClassPath usedClassPath : field.type.getUsedClassPaths()) {
                recipeEventFile.declaration.addClass(usedClassPath);
            }
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return Collections.emptySet();
        }

        val jClassses = new HashSet<Class<?>>();
        for (RecipeTypeJS recipeTypeJS : ALL.values()) {
            jClassses.add(recipeTypeJS.getClass());
        }
        //make sure RecipeEventJS has TSFile generated, to prevent modifyClasses from failing
        jClassses.add(RecipeEventJS.class);
        return jClassses;
    }
}

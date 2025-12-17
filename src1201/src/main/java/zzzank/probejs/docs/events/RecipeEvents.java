package zzzank.probejs.docs.events;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;
import dev.latvian.mods.kubejs.script.ScriptType;
import lombok.val;
import net.minecraft.resources.ResourceLocation;
import zzzank.probejs.features.kubejs.TypeDescAdapter;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.refer.ImportInfo;
import zzzank.probejs.plugin.ProbeJSPlugin;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.Code;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.ts.Statements;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSLambdaType;
import zzzank.probejs.utils.NameUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipeEvents implements ProbeJSPlugin {
    public static final Map<String, ResourceLocation> SHORTCUTS = new HashMap<>();
    public static final ClassPath DOCUMENTED_RECIPES =
        ClassPath.ofArtificial("zzzank.probejs.generated.DocumentedRecipes");

    static {
        SHORTCUTS.put("shaped", new ResourceLocation("kubejs", "shaped"));
        SHORTCUTS.put("shapeless", new ResourceLocation("kubejs", "shapeless"));
        SHORTCUTS.put("smelting", new ResourceLocation("minecraft", "smelting"));
        SHORTCUTS.put("blasting", new ResourceLocation("minecraft", "blasting"));
        SHORTCUTS.put("smoking", new ResourceLocation("minecraft", "smoking"));
        SHORTCUTS.put("campfireCooking", new ResourceLocation("minecraft", "campfire_cooking"));
        SHORTCUTS.put("stonecutting", new ResourceLocation("minecraft", "stonecutting"));
        SHORTCUTS.put("smithing", new ResourceLocation("minecraft", "smithing_transform"));
        SHORTCUTS.put("smithingTrim", new ResourceLocation("minecraft", "smithing_trim"));
    }

    @Override
    public void modifyFiles(RequestAwareFiles files) {
        val scriptDump = files.scriptDump();
        if (scriptDump.scriptType != ScriptType.SERVER) {
            return;
        }
        val converter = scriptDump.transpiler.typeConverter;

        // Generate recipe schema classes
        // Also generate the documented recipe class containing all stuffs from everywhere
        val documentedRecipes = Statements.clazz(DOCUMENTED_RECIPES.getSimpleName());

        for (val entry : RecipeNamespace.getAll().entrySet()) {
            val namespaceId = entry.getKey();
            val namespace = entry.getValue();

            val builder = Types.object();

            for (val e : namespace.entrySet()) {
                val schemaId = e.getKey();
                val schemaType = e.getValue();
                if (schemaType instanceof JsonRecipeSchemaType) {
                    continue;
                }
                val schema = schemaType.schema;
                if (schema == SpecialRecipeSchema.SCHEMA) {
                    continue;
                }

                val schemaPath = getSchemaClassPath(namespaceId, schemaId);
                val schemaDecl = generateSchemaClass(schemaId, schema, converter);
                files.requestOrCreate(schemaPath).addCodes(schemaDecl);

                val recipeFunction = generateSchemaFunction(schemaPath, schema, converter);
                builder.member(schemaId, recipeFunction);
            }

            documentedRecipes.field(namespaceId, builder.build());
        }
        files.requestOrCreate(DOCUMENTED_RECIPES).addCodes(documentedRecipes.build());

        // Inject types into the RecipeEventJS
        val recipeEventFile = files.request(RecipesEventJS.class);
        val recipeEvent = recipeEventFile.findCode(ClassDecl.class).orElse(null);
        if (recipeEvent == null) {
            return; // What???
        }
        recipeEvent.methods.stream()
            .filter(m -> m.params.isEmpty() && m.name.equals("getRecipes"))
            .findFirst()
            .ifPresent(methodDecl -> methodDecl.returnType = Types.type(DOCUMENTED_RECIPES));
        for (Code code : recipeEvent.bodyCode) {
            if (code instanceof BeanDecl beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.type = Types.type(DOCUMENTED_RECIPES);
            }
        }
        recipeEventFile.declaration.addImport(ImportInfo.ofDefault(DOCUMENTED_RECIPES));

        // Make shortcuts valid recipe functions
        for (val code : recipeEvent.bodyCode) {
            if (!(code instanceof BeanDecl.Getter getter)) {
                continue;
            }
            val recipeLocation = SHORTCUTS.get(getter.name);
            if (recipeLocation == null) {
                continue;
            }
            getter.type = Types.format(
                "%s[%s][%s]",
                Types.type(DOCUMENTED_RECIPES),
                Types.literal(recipeLocation.getNamespace()),
                Types.literal(recipeLocation.getPath())
            );
        }
    }

    private static ClassPath getSchemaClassPath(String namespace, String id) {
        return ClassPath.ofArtificial("zzzank.probejs.generated.schema.%s.%s".formatted(
            namespace, NameUtils.rlToTitle(id)
        ));
    }

    /**
     * export class RecipeId {
     * foo(foo: FooType): this
     * bar(bar: BarType): this
     * }
     */
    private static ClassDecl generateSchemaClass(String id, RecipeSchema schema, TypeConverter converter) {
        ClassDecl.Builder builder = Statements.clazz(NameUtils.rlToTitle(id))
            .superClass(Types.type(schema.recipeType));
        for (RecipeKey<?> key : schema.keys) {
            if (key.noBuilders) {
                continue;
            }
            builder.method(
                key.preferred, method -> {
                    method.returnType(Types.THIS);
                    val baseType =
                        TypeDescAdapter.convertType(key.component.constructorDescription(TypeDescAdapter.PROBEJS));
                    if (!baseType.equals(Types.BOOLEAN)) {
                        method.param(key.preferred, baseType);
                    }
                }
            );
        }
        return builder.build();
    }

    private static JSLambdaType generateSchemaFunction(
        ClassPath returnType,
        RecipeSchema schema,
        TypeConverter converter
    ) {
        val builder = Types.lambda()
            .returnType(Types.type(returnType));

        for (RecipeKey<?> key : schema.keys) {
            if (key.excluded) {
                continue;
            }
            builder.param(
                key.preferred,
                TypeDescAdapter.convertType(key.component.constructorDescription(TypeDescAdapter.PROBEJS)),
                key.optional(),
                false
            );
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        val classes = new HashSet<Class<?>>();
        for (val namespace : RecipeNamespace.getAll().values()) {
            for (val schemaType : namespace.values()) {
                classes.add(schemaType.schema.recipeType);
            }
        }
        return classes;
    }
}
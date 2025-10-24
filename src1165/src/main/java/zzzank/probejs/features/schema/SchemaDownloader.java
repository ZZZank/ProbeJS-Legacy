package zzzank.probejs.features.schema;

import dev.latvian.kubejs.KubeJSPaths;
import dev.latvian.kubejs.server.ServerScriptManager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads **safe** schema packs from GitHub
 */
public class SchemaDownloader {
    private static final String GAME_VERSION = "1.21";
    private static final String BASE_URL = "https://github.com/Prunoideae/-recipes/zipball/%s";


    private final Set<String> unsupportedRecipes = new HashSet<>();

    public SchemaDownloader() {
        // Get unsupported script types
        ServerScriptManager manager = ServerScriptManager.instance;
        if (manager == null) return;

//        for (Map.Entry<String, RecipeNamespace> entry : manager.recipeSchemaStorage.namespaces.entrySet()) {
//            String namespace = entry.getKey();
//            RecipeNamespace rn = entry.getValue();
//            for (Map.Entry<String, RecipeSchemaType> e : rn.entrySet()) {
//                String id = e.getKey();
//                RecipeSchemaType schema = e.getValue();
//                if (schema instanceof UnknownRecipeSchemaType) {
//                    unsupportedRecipes.add("%s:%s".formatted(namespace, id));
//                }
//            }
//        }
    }

    public ZipInputStream openSchemaStream() throws URISyntaxException, IOException {
        String downloadUrl = String.format(BASE_URL,GAME_VERSION);
        URL url = (new URI(downloadUrl)).toURL();
        URLConnection connection = url.openConnection();
        return new ZipInputStream(connection.getInputStream());
    }

    public void processFile(ZipInputStream inputStream) throws IOException {
        for (ZipEntry entry = inputStream.getNextEntry(); entry != null; entry = inputStream.getNextEntry()) {
            if (entry.isDirectory()) continue;
            String fileName = entry.getName();
            if (!fileName.endsWith(".json")) continue;
            fileName = fileName.substring(0, fileName.length() - 5);

            // Get the last two components of the entry
            // So ../../minecraft/smoking.json -> minecraft smoking
            String[] parts = fileName.split("/");
            if (parts.length < 2) continue;
            String namespace = parts[parts.length - 2];
            String recipe = parts[parts.length - 1];
            if (!unsupportedRecipes.contains(String.format("%s:%s", namespace, recipe))) continue;

            Path schemaJson = validateSchemaPath(namespace, recipe);
//            try (var writer = new BufferedOutputStream(Files.newOutputStream(schemaJson))) {
//                writer.write(inputStream.readAllBytes());
//            }
        }
    }

    public Path validateSchemaPath(String namespace, String recipe) throws IOException {
        Path dirPath = KubeJSPaths.DATA.resolve(String.format("%s/kubejs/recipe_schemas", namespace));
        if (!Files.exists(dirPath)) Files.createDirectories(dirPath);
        return dirPath.resolve(String.format("%s.json",recipe));
    }
}

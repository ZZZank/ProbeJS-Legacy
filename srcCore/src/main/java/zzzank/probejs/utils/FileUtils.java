package zzzank.probejs.utils;

import com.google.gson.JsonObject;
import lombok.val;
import zzzank.probejs.ProbeJS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FileUtils {
    public static void forEachFile(Path basePath, Consumer<Path> callback) throws IOException {
        try (var dirStream = Files.newDirectoryStream(basePath)) {
            for (Path path : dirStream) {
                if (Files.isDirectory(path)) {
                    forEachFile(path, callback);
                } else {
                    callback.accept(path);
                }
            }
        }
    }

    public static String relativePathStr(Path from, Path to) {
        return from.relativize(to).toString().replace(File.separatorChar, '/');
    }

    public static void writeMergedConfig(Path path, JsonObject config) throws IOException {
        writeMergedConfig(path, config, e -> false);
    }

    /// @param path the path to config
    /// @param config new config
    /// @param replaceIf returns `true` -> original config should be discarded
    public static void writeMergedConfig(Path path, JsonObject config, Predicate<JsonObject> replaceIf) throws IOException {
        JsonObject read = null;
        if (Files.exists(path)) {
            read = ProbeJS.GSON.fromJson(Files.newBufferedReader(path), JsonObject.class);
        }
        if (read == null || replaceIf.test(read)) {
            read = new JsonObject();
        }
        val merged = JsonUtils.mergeJsonRecursively(read, config);
        try (val writer = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path))) {
            writer.setIndent("    ");
            ProbeJS.GSON_WRITER.toJson(merged, JsonObject.class, writer);
        }
    }
}

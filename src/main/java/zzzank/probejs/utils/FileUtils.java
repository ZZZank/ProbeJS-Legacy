package zzzank.probejs.utils;

import com.google.gson.JsonObject;
import dev.latvian.kubejs.KubeJSPaths;
import lombok.val;
import zzzank.probejs.ProbeJS;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
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
        try (val writer = ProbeJS.GSON_WRITER.newJsonWriter(Files.newBufferedWriter(path))) {
            writer.setIndent("    ");
            ProbeJS.GSON_WRITER.toJson(
                JsonUtils.mergeJsonRecursively(read, config),
                JsonObject.class,
                writer
            );
        }
    }

    @Nullable
    public static Path parseSourcePath(String name) {
        if (!name.contains(":")) {
            return null;
        }
        String[] parts = name.split(":", 2);
        Path base = switch (parts[0]) {
            case "client_scripts" -> KubeJSPaths.CLIENT_SCRIPTS;
            case "server_scripts" -> KubeJSPaths.SERVER_SCRIPTS;
            case "startup_scripts" -> KubeJSPaths.STARTUP_SCRIPTS;
            default -> null;
        };
        if (base == null) {
            return null;
        }
        return base.resolve(parts[1]);
    }

    public static long transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[16384];
        int read;
        while ((read = in.read(buffer, 0, 16384)) >= 0) {
            out.write(buffer, 0, read);
            if (transferred < Long.MAX_VALUE) {
                try {
                    transferred = Math.addExact(transferred, read);
                } catch (ArithmeticException ignore) {
                    transferred = Long.MAX_VALUE;
                }
            }
        }
        return transferred;
    }
}

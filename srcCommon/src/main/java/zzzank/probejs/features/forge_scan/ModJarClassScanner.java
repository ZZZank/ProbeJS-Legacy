package zzzank.probejs.features.forge_scan;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author ZZZank
 */
class ModJarClassScanner {

    public static Set<Class<?>> scanJar(Path path) {
        return path == null ? Set.of() : scanJar(path.toFile());
    }

    public static Set<Class<?>> scanJar(File file) {
        try (var jarFile = new JarFile(file)) {
            var modClassesScanner = new ModJarClassScanner(jarFile);
            var scanned = modClassesScanner.scanClasses();
            ProbeJS.LOGGER.info("scanned file '{}', contained class count: {}", file, scanned.size());
            return scanned;
        } catch (Exception e) {
            ProbeJS.LOGGER.error("error when scanning file '{}'", file, e);
        }
        return Collections.emptySet();
    }

    private final JarFile file;
    private final List<String> mixinPackages;

    ModJarClassScanner(JarFile modJar) {
        this.file = modJar;

        // I don't know why will anyone provide a Jar with no manifest file, but it happens
        List<String> mixinPackages = List.of();
        try {
            var rawConfigs = modJar.getManifest().getMainAttributes().getValue("Mixin-Packages");
            if (rawConfigs != null) {
                mixinPackages = readMixinPackages(modJar, rawConfigs.split(","));
            }
        } catch (Exception e) {
            ProbeJS.LOGGER.error("Error when reading mixin config from mod {}", modJar.getName(), e);
        }
        this.mixinPackages = mixinPackages;
    }

    /**
     * @param classPath for example "java/lang/String.class"
     */
    public boolean notInMixinPackage(String classPath) {
        return this.mixinPackages.stream().noneMatch(classPath::startsWith);
    }

    /// Note: the element (mixin package) are in class internal name format: `zzzank/probejs/mixins`, not `zzzank.probejs.mixins`
    private static @NotNull List<String> readMixinPackages(JarFile modJar, String[] mixinConfigsAt) throws IOException {
        var mixinPackages = new TreeSet<String>();
        for (var mixinConfigAt : mixinConfigsAt) {
            try (var in = modJar.getInputStream(modJar.getJarEntry(mixinConfigAt))) {
                var mixinPackage = ProbeJS.GSON.fromJson(new InputStreamReader(in), JsonObject.class)
                    .get("package")
                    .getAsString()
                    .replace('.', '/');
                mixinPackages.add(mixinPackage);
            }
        }
        return List.copyOf(mixinPackages);
    }

    Set<Class<?>> scanClasses() {
        return file.stream()
            .filter(e -> !e.isDirectory())
            .map(ZipEntry::getName)
            .filter(name -> name.endsWith(ReflectUtils.CLASS_SUFFIX))
            .filter(this::notInMixinPackage)
            .map(name -> name.substring(0, name.length() - ReflectUtils.CLASS_SUFFIX.length()).replace("/", "."))
            .filter(ReflectUtils.NOT_ARTIFICIAL_CLASS)
            .map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .filter(c -> Modifier.isPublic(c.getModifiers()) || !ProbeConfig.publicClassOnly.get())
            .collect(Collectors.toSet());
    }
}

package zzzank.probejs.features.forge_scan;

import com.google.gson.JsonObject;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * @author ZZZank
 */
class ModJarClassScanner {

    public static Set<Class<?>> scanFile(File file) {
        try (val jarFile = new JarFile(file)) {
            val modClassesScanner = new ModJarClassScanner(jarFile);
            val scanned = modClassesScanner.scanClasses();
            ProbeJS.LOGGER.info("scanned file '{}', contained class count: {}", file.getName(), scanned.size());
            return scanned;
        } catch (Exception e) {
            ProbeJS.LOGGER.error("error when scanning file '{}'", file.getName(), e);
        }
        return Collections.emptySet();
    }

    private final JarFile file;
    private final List<String> mixinPackages;

    ModJarClassScanner(JarFile modJar) throws IOException {
        this.file = modJar;

        var mixinConfigsAt = modJar.getManifest()
            .getMainAttributes()
            .getValue("MixinConfigs");
        var mixinConfigs = mixinConfigsAt == null ? new String[0] : mixinConfigsAt.split(",");
        this.mixinPackages = readMixinPackages(modJar, mixinConfigs);
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
            .map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .filter(c -> Modifier.isPublic(c.getModifiers()) || !ProbeConfig.publicClassOnly.get())
            .collect(Collectors.toSet());
    }
}

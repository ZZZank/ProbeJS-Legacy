package zzzank.probejs.features.forge_scan;

import lombok.val;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.ReflectUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author ZZZank
 */
class ModJarClassScanner {

    public static final String DESC_MIXIN = Type.getDescriptor(Mixin.class);
    public static final String CLASS_SUFFIX = ".class";

    public static Set<Class<?>> scanFile(File file) {
        try (val jarFile = new ZipFile(file)) {
            val modClassesScanner = new ModJarClassScanner(jarFile);
            val scanned = modClassesScanner.scanClasses();
            ProbeJS.LOGGER.info("scanned file '{}', contained class count: {}", file.getName(), scanned.size());
            return scanned;
        } catch (Exception e) {
            ProbeJS.LOGGER.error("error when scanning file '{}'", file.getName(), e);
        }
        return Collections.emptySet();
    }

    private final ZipFile file;
    private final ClassLoader classLoader;

    ModJarClassScanner(ZipFile modJar) {
        this.file = modJar;
        this.classLoader = this.getClass().getClassLoader();
    }

    /**
     * @param classPath for example "java/lang/String.class"
     */
    public boolean presentAndNotMixin(String classPath) {
        return presentAndNotMixin(classLoader, classPath);
    }

    /**
     * @param classLoader
     * @param classPath for example "java/lang/String.class"
     */
    public static boolean presentAndNotMixin(ClassLoader classLoader, String classPath) {
        val resource = classLoader.getResourceAsStream(classPath);
        if (resource == null) {
            // no such class
            return false;
        }
        try {
            val reader = new ClassReader(resource);
            val visitor = new ProbeClassVisitor();
            reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            for (val annotation : visitor.visibleAnnotations) {
                if (DESC_MIXIN.equals(annotation.desc)) {
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    Set<Class<?>> scanClasses() {
        return file.stream()
            .filter(e -> !e.isDirectory())
            .map(ZipEntry::getName)
            .filter(name -> name.endsWith(CLASS_SUFFIX))
            .filter(this::presentAndNotMixin)
            .map(name -> name.substring(0, name.length() - CLASS_SUFFIX.length()).replace("/", "."))
            .map(ReflectUtils::classOrNull)
            .filter(Objects::nonNull)
            .filter(c -> Modifier.isPublic(c.getModifiers()) || !ProbeConfig.publicClassOnly.get())
            .collect(Collectors.toSet());
    }
}

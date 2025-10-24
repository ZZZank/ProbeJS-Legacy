package zzzank.probejs.lang.java;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.lang.java.clazz.MemberCollector;
import zzzank.probejs.utils.ReflectUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@HideFromJS
public class ClassRegistry {
    public static final ClassRegistry REGISTRY = new ClassRegistry();

    public final Map<ClassPath, Clazz> foundClasses = new ConcurrentHashMap<>();
    public final MemberCollector collector;

    public ClassRegistry() {
        this(new ClazzMemberCollector());
    }

    public ClassRegistry(MemberCollector memberCollector) {
        collector = memberCollector;
    }

    public void fromClazz(Collection<Clazz> classes) {
        for (val c : classes) {
            foundClasses.putIfAbsent(c.classPath, c);
        }
    }

    public List<Clazz> addClasses(Collection<Class<?>> classes) {
        return classes
            .stream()
            .map(this::addClass)
            .filter(Objects::nonNull)
            .toList();
    }

    public Clazz addClass(Class<?> c) {
        return addClassImpl(c, ClassPath.fromJava(c));
    }

    public Clazz addClass(ClassPath path) {
        return addClassImpl(ReflectUtils.classOrNull(path.getJavaPath(), ProbeJS.LOGGER::error), path);
    }

    /**
     * @param c the class to be added to class registry
     * @return the {@link Clazz} object corresponds to the provided parameter {@code c}, or {@code null} if the class
     * fail to pass {@link #classPrefilter(Class)} or exceptions happen
     */
    public Clazz addClassImpl(Class<?> c, ClassPath path) {
        if (c == null || c.isPrimitive() || c.isSynthetic() || c.isAnonymousClass()) {
            return null;
        }
        try {
            return foundClasses.computeIfAbsent(path, p -> new Clazz(c, p, collector));
        } catch (Throwable ex) {
            ProbeJS.LOGGER.error("Error when trying to add '{}' into class registry", c, ex);
            return null;
        }
    }

    public boolean classPrefilter(Class<?> c) {
        return ReflectUtils.classExist(c.getName()) && !c.isSynthetic() && !c.isAnonymousClass() && !c.isPrimitive();
    }

    private Set<Class<?>> retrieveClass(Clazz clazz) {
        Set<Class<?>> classes = new HashSet<>();

        for (val constructor : clazz.constructors) {
            for (val param : constructor.params) {
                classes.addAll(param.type.getClasses());
            }
            for (val variableType : constructor.variableTypes) {
                classes.addAll(variableType.getClasses());
            }
        }

        for (val method : clazz.methods) {
            for (val param : method.params) {
                classes.addAll(param.type.getClasses());
            }
            for (val variableType : method.variableTypes) {
                classes.addAll(variableType.getClasses());
            }
            classes.addAll(method.returnType.getClasses());
        }

        for (val field : clazz.fields) {
            classes.addAll(field.type.getClasses());
        }

        for (val variableType : clazz.variableTypes) {
            classes.addAll(variableType.getClasses());
        }

        if (clazz.superClass != null) {
            classes.addAll(clazz.superClass.getClasses());
        }
        for (val i : clazz.interfaces) {
            classes.addAll(i.getClasses());
        }

        return classes;
    }

    public void walkClass() {
        Collection<Clazz> toWalk = new HashSet<>(foundClasses.values());
        val walked = new HashSet<>(this.foundClasses.values());

        while (!toWalk.isEmpty()) {
            ProbeJS.LOGGER.info("walking {} newly discovered classes", toWalk.size());

            toWalk = toWalk.stream()
                .map(this::retrieveClass)
                .flatMap(Collection::stream)
                .distinct() // deduplicate
                .map(this::addClass) // class adding happens here
                .filter(Objects::nonNull)
                .filter(walked::add)
                .collect(Collectors.toSet());
        }
    }

    public Collection<Clazz> getFoundClasses() {
        return foundClasses.values();
    }

    public void writeTo(Path path) throws IOException {
        val classPaths = new ArrayList<>(foundClasses.keySet());
        Collections.sort(classPaths);

        var lastPath = ClassPath.EMPTY;
        try (val writer = Files.newBufferedWriter(path)) {
            for (val classPath : classPaths) {
                val commonPartsCount = classPath.getCommonPartsCount(lastPath);
                val copy = new ArrayList<>(classPath.getParts());
                Collections.fill(copy.subList(0, commonPartsCount), "");
                writer.write(String.join(".", copy));
                writer.write('\n');
                lastPath = classPath;
            }
        }
    }

    public void loadFrom(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        var lastPath = ClassPath.EMPTY;
        try (val reader = Files.newBufferedReader(path)) {
            for (val className : (Iterable<String>) reader.lines()::iterator) {
                val parts = className.split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        break;
                    }
                    parts[i] = lastPath.getPart(i);
                }
                val classPath = ClassPath.fromRaw(String.join(".", parts));
                if (!this.foundClasses.containsKey(classPath)) {
                    try {
                        val c = Class.forName(
                            classPath.getJavaPath(),
                            false,
                            ClassRegistry.class.getClassLoader()
                        );
                        if (!ProbeConfig.publicClassOnly.get() || Modifier.isPublic(c.getModifiers())) {
                            addClass(c);
                        }
                    } catch (Throwable ex) {
                        ProbeJS.LOGGER.error(
                            "Error when loading class '{}' from cache file: {}",
                            className,
                            ex.toString()
                        );
                    }
                }
                lastPath = classPath;
            }
        } catch (IOException ex) {
            ProbeJS.LOGGER.error("Error when loading classes from cache file: {}", ex.toString());
        }
    }
}

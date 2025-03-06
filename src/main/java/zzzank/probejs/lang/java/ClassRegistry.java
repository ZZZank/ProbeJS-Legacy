package zzzank.probejs.lang.java;

import dev.latvian.mods.rhino.util.HideFromJS;
import lombok.val;
import zzzank.probejs.ProbeConfig;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.lang.java.clazz.MemberCollector;
import zzzank.probejs.utils.CollectUtils;
import zzzank.probejs.utils.ReflectUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@HideFromJS
public class ClassRegistry {
    public static final ClassRegistry REGISTRY = new ClassRegistry(new ClazzMemberCollector());

    public final Map<ClassPath, Clazz> foundClasses = new HashMap<>(256);
    public final MemberCollector collector;

    public ClassRegistry(MemberCollector memberCollector) {
        collector = memberCollector;
    }

    public void fromClazz(Collection<Clazz> classes) {
        for (val c : classes) {
            foundClasses.putIfAbsent(c.classPath, c);
        }
    }

    public List<Clazz> fromClasses(Collection<Class<?>> classes) {
        return classes
            .stream()
            .map(this::fromClass)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * @param c the class to be added class registry
     * @return {@code true} if the class was not added to class registry before, {}
     */
    public Clazz fromClass(Class<?> c) {
        if (!classPrefilter(c)) {
            // We test if the class actually exists from forName
            // I think some runtime class can have non-existing Class<?> object due to .getSuperClass
            // or .getInterfaces
            return null;
        }
        try {
            return foundClasses.computeIfAbsent(
                ClassPath.fromJava(c),
                k -> new Clazz(c, collector)
            );
        } catch (Throwable ignored) {
            return null;
        }
    }

    public boolean classPrefilter(Class<?> c) {
        return ReflectUtils.classExist(c.getName()) && !c.isSynthetic() && !c.isAnonymousClass() && !c.isPrimitive();
    }

    private Set<Class<?>> retrieveClass(Clazz clazz) {
        Set<Class<?>> classes = CollectUtils.identityHashSet();

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
        var classesToWalk = new HashSet<>(foundClasses.values());

        while (!classesToWalk.isEmpty()) {
            ProbeJS.LOGGER.debug("walking {} newly discovered classes", classesToWalk.size());
            val collected = classesToWalk
                .stream()
                .map(this::retrieveClass)
                .flatMap(Collection::stream)
                .collect(Collectors.toCollection(CollectUtils::identityHashSet));
            classesToWalk = new HashSet<>();

            for (val c : collected) {
                val clazz = fromClass(c);
                if (clazz == null || foundClasses.containsKey(clazz.classPath)) {
                    continue;
                }
                classesToWalk.add(clazz);
            }
        }
    }

    public Collection<Clazz> getFoundClasses() {
        return foundClasses.values();
    }

    public void writeTo(Path path) throws IOException {
        val classPaths = new ArrayList<>(foundClasses.keySet());
        Collections.sort(classPaths);

        var lastPath = new ClassPath(new String[0]);
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
        var lastPath = new ClassPath(new String[0]);
        try (val reader = Files.newBufferedReader(path)) {
            for (val className : (Iterable<String>) reader.lines()::iterator) {
                val parts = className.split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    if (!parts[i].isEmpty()) {
                        break;
                    }
                    parts[i] = lastPath.getPart(i);
                }
                val classPath = new ClassPath(parts);
                try {
                    val c= ReflectUtils.classOrNull(classPath.getJavaPath());
                    if (!ProbeConfig.publicClassOnly.get() || Modifier.isPublic(c.getModifiers())) {
                        fromClass(c);
                    }
                } catch (Throwable ignored) {
                }
                lastPath = classPath;
            }
        } catch (IOException ignored) {
        }
    }
}

package zzzank.probejs.utils;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author ZZZank
 */
public interface ReflectUtils {
    String CLASS_SUFFIX = ".class";

    static Constructor<?>[] constructorsSafe(Class<?> c) {
        try {
            return c.getConstructors();
        } catch (Throwable e) {
            return new Constructor[0];
        }
    }

    static Field[] fieldsSafe(Class<?> c) {
        try {
            return c.getFields();
        } catch (Throwable e) {
            return new Field[0];
        }
    }

    static Field[] declaredFieldsSafe(Class<?> c) {
        try {
            return c.getDeclaredFields();
        } catch (Throwable e) {
            return new Field[0];
        }
    }

    static Method[] methodsSafe(Class<?> c) {
        try {
            return c.getMethods();
        } catch (Throwable e) {
            return new Method[0];
        }
    }

    static Class<?> classOrNull(String name, ClassLoader loader, boolean initialize, @Nullable Consumer<String> errorReporter) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (Throwable e) {
            if (errorReporter != null) {
                errorReporter.accept(String.format("error loading class with name '%s':%s", name , e));
            }
        }
        return null;
    }

    static Class<?> classOrNull(String name, boolean initialize, @Nullable Consumer<String> errorReporter) {
        return classOrNull(name, ReflectUtils.class.getClassLoader(), initialize, errorReporter);
    }

    static Class<?> classOrNull(String name, Consumer<String> errorReporter) {
        return classOrNull(name, ReflectUtils.class.getClassLoader(), false, errorReporter);
    }

    static Class<?> classOrNull(String name) {
        return classOrNull(name, ReflectUtils.class.getClassLoader(), false, null);
    }

    static boolean classExist(String name) {
        return classOrNull(name) != null;
    }

    static AssertionError unreachable() {
        return new AssertionError("Unreachable code");
    }

    static AssertionError unreachable(Throwable cause) {
        return new AssertionError("Unreachable code", cause);
    }

    /// For these examples, this method return `true`:
    /// - `Outer$1`
    /// - `Outer$123`
    /// - `Outer$Inner$111`
    /// - `Outer$1Local`
    ///
    /// For these examples, this method return `false`:
    /// - `Outer`
    /// - `Outer$`
    /// - `$Outer`
    /// - `Outer$Inner`
    /// - `Outer$Inner123`
    ///
    /// @return `true` if there's a number digit after the last `$`, `false` otherwise
    static boolean isArtificialClass(String className) {
        var lastSep = className.lastIndexOf('$');
        if (lastSep < 0) {
            return false;
        }
        var innerName = className.substring(lastSep + 1);
        return !innerName.isEmpty() && Character.isDigit(innerName.charAt(0));
    }

    /// return the opposite of [#isArtificialClass(java.lang.String)]
    Predicate<String> NOT_ARTIFICIAL_CLASS = name -> !isArtificialClass(name);
}

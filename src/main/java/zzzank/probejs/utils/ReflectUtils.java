package zzzank.probejs.utils;

import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    static Class<?> classOrNull(String name, ClassLoader loader, boolean initialize, @Nullable Logger errorReporter) {
        try {
            return Class.forName(name, initialize, loader);
        } catch (Throwable e) {
            if (errorReporter != null) {
                errorReporter.error("error loading class with name '{}': {}", name , e.getLocalizedMessage());
            }
        }
        return null;
    }

    static Class<?> classOrNull(String name, boolean initialize, @Nullable Logger errorReporter) {
        return classOrNull(name, ReflectUtils.class.getClassLoader(), initialize, errorReporter);
    }

    static Class<?> classOrNull(String name, Logger errorReporter) {
        return classOrNull(name, false, errorReporter);
    }

    static Class<?> classOrNull(String name) {
        return classOrNull(name, null);
    }

    static boolean classExist(String name) {
        return classOrNull(name) != null;
    }
}

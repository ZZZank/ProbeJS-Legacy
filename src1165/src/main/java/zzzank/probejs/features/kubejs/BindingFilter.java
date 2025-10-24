package zzzank.probejs.features.kubejs;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ZZZank
 */
public class BindingFilter {
    public final Set<String> classNames = new HashSet<>();
    public final Set<String> constantNames = new HashSet<>();
    public final Set<String> functionNames = new HashSet<>();

    /**
     * @return {@code true} if NOT already denied
     */
    public boolean denyClass(String name) {
        return classNames.add(name);
    }

    /**
     * @return {@code true} if NOT already denied
     */
    public boolean denyConstant(String name) {
        return constantNames.add(name);
    }

    /**
     * @return {@code true} if NOT already denied
     */
    public boolean denyFunction(String name) {
        return functionNames.add(name);
    }

    public boolean isClassDenied(String name) {
        return classNames.contains(name);
    }

    public boolean isConstantDenied(String name) {
        return constantNames.contains(name);
    }

    public boolean isFunctionDenied(String name) {
        return functionNames.contains(name);
    }
}

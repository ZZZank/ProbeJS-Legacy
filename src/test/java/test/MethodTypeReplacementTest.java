package test;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.VariableType;
import zzzank.probejs.lang.transpiler.Transpiler;

import java.io.IOException;
import java.lang.reflect.TypeVariable;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class MethodTypeReplacementTest {
    private static final ClassRegistry CLASS_REGISTRY = new ClassRegistry();
    private static final Transpiler TRANSPILER = new Transpiler();

    static {
        InitFMLPathsTest.init();
        TRANSPILER.init();
    }

    @Test
    public void unaryOp() {
        val classPath = ClassPath.fromJava(UnaryOperator.class);

        CLASS_REGISTRY.addClass(UnaryOperator.class);
        CLASS_REGISTRY.walkClass();
        val clazz = CLASS_REGISTRY.foundClasses.get(classPath);

        clazz.methods
            .stream()
            .flatMap(method -> Stream.concat(
                Stream.of(method.returnType),
                method.params.stream().map(p -> p.type)
            ))
            .flatMap(TypeDescriptor::stream)
            .map(t -> t instanceof VariableType variableType ? variableType : null)
            .filter(Objects::nonNull)
            .map(VariableType::raw)
            .map(TypeVariable::getGenericDeclaration)
            .map(d -> d instanceof Class<?> c ? c : null)
            .filter(Objects::nonNull)
            .forEach(c -> Assertions.assertEquals(UnaryOperator.class, c));
    }
}

package test;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.impl.VariableType;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class MethodTypeReplacementTest {

    static {
        InitFMLPathsTest.init();
    }

    @ParameterizedTest
    @ValueSource(classes = {UnaryOperator.class, Collection.class, StringIterable.class, StrList.class})
    public void test(Class<?> type) {
        val classRegistry = new ClassRegistry();

        classRegistry
            .addClass(type)
            .methods
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
            .forEach(c -> Assertions.assertEquals(type, c));
    }

    interface StringIterable extends Iterable<String> {}

    static final class StrList extends ArrayList<String> {}
}

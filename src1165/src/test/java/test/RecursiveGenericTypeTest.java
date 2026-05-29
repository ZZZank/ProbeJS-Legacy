package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.BasicMemberCollector;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.transpiler.Transpiler;
import zzzank.probejs.lang.transpiler.TypeConverter;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.ts.TSClassType;
import zzzank.probejs.lang.typescript.code.type.ts.TSParamType;
import zzzank.probejs.lang.typescript.code.type.ts.TSVariableType;

/**
 * @author ZZZank
 */
public class RecursiveGenericTypeTest {

    @Test
    public void test() {
        var classRegistry = new ClassRegistry(new BasicMemberCollector());
        classRegistry.addClass(Enum.class);
        classRegistry.walkClass();

        var transpiler = new Transpiler(new TypeConverter());
        var files = transpiler.dump(classRegistry.getFoundClasses());

        var classDecl = files.get(ClassPath.ofJava(Enum.class))
            .findCode(ClassDecl.class)
            .orElseThrow();

        var variableTypes = classDecl.variableTypes;
        Assertions.assertEquals(1, variableTypes.size());

        // expected: E extends Enum<E> = Enum<any>
        var variableType = variableTypes.getFirst();
        Assertions.assertEquals("E", variableType.symbol);
        Assertions.assertTrue(
            variableType.extend instanceof TSParamType paramExt
            && paramExt.baseType instanceof TSClassType paramExtBase
            && paramExtBase.classPath.equals(ClassPath.ofJava(Enum.class))
            && paramExt.params.size() == 1
            && paramExt.params.getFirst() instanceof TSVariableType paramExtVariable
            && paramExtVariable.symbol.equals("E"), "variableType should have extend bound (Enum<E>)"
        );
        Assertions.assertTrue(
            variableType.defaultTo instanceof TSParamType paramDef
            && paramDef.baseType instanceof TSClassType paramDefBase
            && paramDefBase.classPath.equals(ClassPath.ofJava(Enum.class))
            && paramDef.params.size() == 1
            && paramDef.params.getFirst() == Types.ANY, "variableType should have default type (Enum<any>)"
        );
    }
}

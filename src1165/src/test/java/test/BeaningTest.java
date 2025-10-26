package test;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.ClassDecl;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.plugin.ProbeJSPlugins;

/**
 * @author ZZZank
 */
public class BeaningTest {

    static {
        InitFMLPathsTest.init();
    }

    @ParameterizedTest
    @ValueSource(classes = {Interf.class, AbsCls.class})
    public void test(Class<?> type) {
        val classRegistry = new ClassRegistry(new ClazzMemberCollector());
        classRegistry.addClass(type);
        classRegistry.walkClass();

        val transpiler = ProbeJSPlugins.buildTranspiler();

        val files = transpiler.dump(classRegistry.getFoundClasses());
        val classDecl = files.get(ClassPath.fromJava(type))
            .findCode(ClassDecl.class)
            .orElseThrow();

        var hasMatchingBean = false;
        for (var code : classDecl.bodyCode) {
            if (code instanceof BeanDecl beanDecl) {
                switch (beanDecl.name) {
                    case "" -> throw new AssertionError("empty");
                    case "Str" -> throw new AssertionError("Str");
                    case "str" -> {
                        hasMatchingBean = true;
                        Assertions.assertEquals(
                            "string",
                            ((JSPrimitiveType) beanDecl.type).content
                        );
                    }
                }
            }
        }
        Assertions.assertTrue(hasMatchingBean);
    }

    interface Interf {

        int get();

        void set(int value);

        String getStr();

        void setStr(String value);
    }

    static abstract class AbsCls {

        public abstract int get();

        public abstract void set(int value);

        public abstract String getStr();

        public abstract void setStr(String value);
    }
}

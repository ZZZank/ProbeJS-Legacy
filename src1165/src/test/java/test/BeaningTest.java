package test;

import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import zzzank.probejs.docs.InjectBeaning;
import zzzank.probejs.lang.java.ClassRegistry;
import zzzank.probejs.lang.java.clazz.ClassPath;
import zzzank.probejs.lang.java.clazz.ClazzMemberCollector;
import zzzank.probejs.lang.typescript.RequestAwareFiles;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.SharedDump;
import zzzank.probejs.lang.typescript.code.member.BeanDecl;
import zzzank.probejs.lang.typescript.code.member.BeanFormattedFieldDecl;
import zzzank.probejs.lang.typescript.code.member.InterfaceDecl;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import zzzank.probejs.plugin.ProbeJSPlugins;

import java.util.stream.Collectors;

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
        new InjectBeaning().modifyFiles(new RequestAwareFiles(files, ScriptDump.STARTUP_DUMP.apply(new SharedDump(
            null, classRegistry, transpiler
        ))));

        var classDecl = files.get(ClassPath.ofJava(type))
            .asNative()
            .nativeClass;

        var hasMatchingBean = false;

        Assertions.assertEquals(2, classDecl.bodyCode.size());
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

    @Test
    public void testFieldAsBeaning() {
        val classRegistry = new ClassRegistry(new ClazzMemberCollector());
        classRegistry.addClass(FieldAsBeaning.class);
        classRegistry.walkClass();

        val transpiler = ProbeJSPlugins.buildTranspiler();

        val files = transpiler.dump(classRegistry.getFoundClasses());
        new InjectBeaning().modifyFiles(new RequestAwareFiles(files, ScriptDump.STARTUP_DUMP.apply(new SharedDump(
            null, classRegistry, transpiler
        ))));

        var file = files.get(ClassPath.ofJava(FieldAsBeaning.class)).asNative();

        var fields = file.nativeClass.fields;
        Assertions.assertEquals(1, fields.stream().filter(f -> f instanceof BeanFormattedFieldDecl).count());

        var field = (BeanFormattedFieldDecl) fields.stream()
            .filter(f -> f instanceof BeanFormattedFieldDecl)
            .findFirst()
            .orElseThrow();
        Assertions.assertEquals("string", field.name);
        Assertions.assertEquals(Types.STRING, field.type);

        var lines = file.format()
            .stream()
            .map(String::strip)
            .collect(Collectors.joining("\n"));
        Assertions.assertTrue(lines.contains("""
            get "string"(): string
            set "string"(value: string)
            """));
    }

    @SuppressWarnings("unused")
    interface Interf {

        int get();

        void set(int value);

        String getStr();

        void setStr(String value);
    }

    @SuppressWarnings("unused")
    static abstract class AbsCls {

        public abstract int get();

        public abstract void set(int value);

        public abstract String getStr();

        public abstract void setStr(String value);
    }

    static abstract class FieldAsBeaning {

        public static final int staticFinalInt = 1;
        public static int staticInt = 1;

        public final String finalString = "";
        public String string = "";
    }
}

package test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import zzzank.probejs.lang.java.clazz.BasicMemberCollector;

/**
 * @see BasicMemberCollector
 * @author ZZZank
 */
public class MethodFilteringTest {

    @Test
    public void test() {
        var memberCollector = new BasicMemberCollector();
        memberCollector.accept(ClazzC.class);

        var methods = memberCollector.methods().toList();
//        for (var method : methods) {
//            System.out.println(method);
//        }

        var names = methods.stream().map(m -> m.name).toList();
        Assertions.assertTrue(names.contains("modifyReturnType"));
        Assertions.assertTrue(names.contains("nonPublicSuper"));
        Assertions.assertTrue(names.contains("implementInterface"));
        Assertions.assertTrue(names.contains("interfaceOnly"));
        Assertions.assertTrue(names.contains("objectOverriddenByGeneric"));
        Assertions.assertFalse(names.contains("generic"));
        Assertions.assertFalse(names.contains("bothInterfaceAndAbstract"));
    }

    public interface InterfaceA {

        Object generic();

        CharSequence modifyReturnType();

        CharSequence modifyReturnType(String seq1);

        boolean interfaceOnly();

        Boolean implementInterface(boolean bool);

        void bothInterfaceAndAbstract(String... str);
    }

    public static abstract class ClassB<B> {

        public B generic() {
            return null;
        }

        public abstract Object objectOverriddenByGeneric();

        abstract CharSequence nonPublicSuper(CharSequence seq1);

        public abstract CharSequence modifyReturnType(String seq1);

        public abstract Object sameSignature(Number num);

        public abstract void bothInterfaceAndAbstract(String... str);
    }

    public static abstract class ClazzC<C> extends ClassB<String> implements InterfaceA {
        public String modifyReturnType() {
            return null;
        }

        @Override
        public C objectOverriddenByGeneric() {
            return null;
        }

        @Override
        public String modifyReturnType(String seq1) {
            return null;
        }

        @Override
        public Object sameSignature(Number num) {
            return null;
        }

        @Override
        public CharSequence nonPublicSuper(CharSequence seq1) {
            return null;
        }

        @Override
        public Boolean implementInterface(boolean bool) {
            return null;
        }
    }
}

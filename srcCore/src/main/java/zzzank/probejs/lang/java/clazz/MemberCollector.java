package zzzank.probejs.lang.java.clazz;

import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;

import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public interface MemberCollector {
    MemberCollector BASIC = new BasicMemberCollector();
    MemberCollector DEFAULT = ServiceLoader.load(MemberCollector.class)
        .findFirst()
        .orElse(BASIC);

    void accept(Class<?> clazz);

    Stream<ConstructorInfo> constructors();

    Stream<MethodInfo> methods();

    Stream<FieldInfo> fields();
}

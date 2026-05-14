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
    MemberCollector DEFAULT = ServiceLoader.load(MemberCollector.class)
        .findFirst()
        .orElseGet(BasicMemberCollector::new);

    Class<?> currentTarget();

    /// Get a new [MemberCollector] that targets the specified `clazz`. A [MemberCollector] instance with no
    /// [#reTarget(java.lang.Class)] calls should target the [Object] class
    MemberCollector reTarget(Class<?> clazz);

    Stream<ConstructorInfo> constructors();

    Stream<MethodInfo> methods();

    Stream<FieldInfo> fields();
}

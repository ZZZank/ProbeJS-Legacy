package zzzank.probejs.lang.java.clazz;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.rhino.JavaMembers;
import lombok.val;
import zzzank.probejs.lang.java.clazz.members.ConstructorInfo;
import zzzank.probejs.lang.java.clazz.members.FieldInfo;
import zzzank.probejs.lang.java.clazz.members.MethodInfo;
import zzzank.probejs.lang.java.type.TypeDescriptor;
import zzzank.probejs.lang.java.type.TypeReplacementCollector;
import zzzank.probejs.lang.java.type.impl.VariableType;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public class ClazzMemberCollector implements MemberCollector {

    private final Class<?> clazz;
    private final JavaMembers members;
    private final TypeReplacementCollector typeReplacementCollector;
    private final Map<VariableType, TypeDescriptor> typeReplacement;

    public ClazzMemberCollector() {
        this(Object.class, new TypeReplacementCollector());
    }

    public ClazzMemberCollector(Class<?> clazz, TypeReplacementCollector typeReplacementCollector) {
        this.clazz = clazz;
        val scriptManager = KubeJS.getStartupScriptManager();
        this.members = JavaMembers.lookupClass(
            scriptManager.context,
            scriptManager.topLevelScope,
            clazz,
            clazz,
            false
        );
        this.typeReplacementCollector = typeReplacementCollector;
        this.typeReplacement = typeReplacementCollector.getTypeReplacement(clazz);
    }

    @Override
    public Class<?> currentTarget() {
        return clazz;
    }

    @Override
    public MemberCollector reTarget(Class<?> clazz) {
        return new ClazzMemberCollector(clazz, typeReplacementCollector);
    }

    @Override
    public Stream<ConstructorInfo> constructors() {
        return members.getAccessibleConstructors()
            .stream()
            .map(ConstructorInfo::new);
    }

    @Override
    public Stream<MethodInfo> methods() {
        return members.getAccessibleMethods(KubeJS.getStartupScriptManager().context, false)
            .stream()
            .filter(m -> !m.method.isSynthetic())
            .filter(m -> BasicMemberCollector.filterInherited(m.method, clazz))
            .map(info -> new MethodInfo(info.method, info.name, typeReplacement))
            .sorted(MethodInfo.commonComparator());
    }

    @Override
    public Stream<FieldInfo> fields() {
        return members.getAccessibleFields(KubeJS.getStartupScriptManager().context, false)
            .stream()
            // those not declared by it will be inherited from super
            .filter(info -> info.field.getDeclaringClass() == this.clazz)
            .map(info -> new FieldInfo(info.field, info.name))
            .sorted(FieldInfo.commonComparator());
    }
}

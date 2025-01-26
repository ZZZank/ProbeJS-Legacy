package zzzank.probejs.docs.assignments;

import dev.latvian.mods.rhino.native_java.type.info.EnumTypeInfo;
import lombok.val;
import zzzank.probejs.features.rhizo.RhizoState;
import zzzank.probejs.lang.java.clazz.Clazz;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.lang.typescript.code.type.BaseType;
import zzzank.probejs.lang.typescript.code.type.Types;
import zzzank.probejs.plugin.ProbeJSPlugin;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class EnumTypes implements ProbeJSPlugin {
    // EnumTypeWrapper is not thread-safe
    private static final ReentrantLock LOCK = new ReentrantLock();

    @Override
    public void assignType(ScriptDump scriptDump) {
        if (!RhizoState.ENUM_TYPE_WRAPPER && !RhizoState.ENUM_TYPE_INFO) {
            return;
        }
        LOCK.lock();
        for (val recordedClass : scriptDump.recordedClasses) {
            if (recordedClass.attribute.type != Clazz.ClassType.ENUM) {
                continue;
            }
            try {
                val types = Arrays.stream(recordedClass.getOriginal().getEnumConstants())
                    .map(EnumTypes::getEnumName)
                    .filter(Objects::nonNull)
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .map(Types::literal)
                    .toArray(BaseType[]::new);
                scriptDump.assignType(recordedClass.classPath, Types.or(types));
            } catch (Throwable ignore) {
            }
        }
        LOCK.unlock();
    }

    private static String getEnumName(Object o) {
        if (RhizoState.ENUM_TYPE_INFO) {
            return EnumTypeInfo.getName(o);
        }
        if (o instanceof Enum<?> e) {
            return e.name();
        }
        return null;
    }
}

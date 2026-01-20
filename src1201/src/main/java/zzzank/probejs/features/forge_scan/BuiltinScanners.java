package zzzank.probejs.features.forge_scan;

import lombok.val;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public enum BuiltinScanners {
    NONE {
        @Override
        public Stream<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            return Stream.empty();
        }
    },
    FULL {
        @Override
        public Stream<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            return dataStream
                .map(ModFileScanData.ClassData::clazz)
                .map(Type::getClassName);
        }
    },
    EVENTS {
        @Override
        public Stream<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            val names = new HashSet<>(PREDEFINED_BASECLASS);

            val queue = new ArrayDeque<>(PREDEFINED_BASECLASS);
            val toSubClasses = new HashMap<String, List<String>>();
            for (var data : CollectUtils.iterate(dataStream)) {
                toSubClasses
                    .computeIfAbsent(data.parent().getClassName(), CollectUtils.computeArrayList3())
                    .add(data.clazz().getClassName());
            }

            while (!queue.isEmpty()) {
                val parent = queue.pop();
                names.add(parent);
                val subClasses = toSubClasses.get(parent);
                if (subClasses != null) {
                    queue.addAll(subClasses);
                }
            }

            return names.stream();
        }
    };

    /**
     * will only be used by {@link BuiltinScanners#EVENTS}
     */
    public static final Set<String> PREDEFINED_BASECLASS = new HashSet<>();

    /**
     * stream of class data -> class name
     */
    public abstract Stream<String> scan(Stream<ModFileScanData.ClassData> dataStream);
}

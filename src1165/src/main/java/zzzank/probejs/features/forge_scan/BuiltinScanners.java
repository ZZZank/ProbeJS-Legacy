package zzzank.probejs.features.forge_scan;

import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.stream.Collectors;
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
            return dataStream.map(AccessClassData::clazz).map(Type::getClassName);
        }
    },
    EVENTS {
        @Override
        public Stream<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            var names = new HashSet<String>();

            var queue = PREDEFINED_BASECLASS.stream()
                .map(name -> Type.getObjectType(name.replace('.', '/')))
                .collect(Collectors.toCollection(ArrayDeque::new));
            var byParent = dataStream.collect(Collectors.groupingBy(AccessClassData::parent));

            while (!queue.isEmpty()) {
                var parentType = queue.pop();
                names.add(parentType.getClassName());
                for (var subClass : byParent.getOrDefault(parentType, List.of())) {
                    queue.add(AccessClassData.clazz(subClass));
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

package zzzank.probejs.features.forge_scan;

import com.google.common.collect.ArrayListMultimap;
import lombok.val;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.forgespi.language.ModFileScanData;
import zzzank.probejs.ProbeJS;
import zzzank.probejs.utils.CollectUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author ZZZank
 */
public enum BuiltinScanners implements ClassDataScanner {
    NONE {
        @Override
        public Collection<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            return Collections.emptyList();
        }
    },
    FULL {
        @Override
        public Collection<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            val collected = dataStream
                .map(AccessClassData::new)
                .map(AccessClassData::className)
                .toList();
            ProbeJS.LOGGER.debug("FullScan collected {} class names", collected.size());
            return collected;
        }
    },
    EVENTS {
        @Override
        public Collection<String> scan(Stream<ModFileScanData.ClassData> dataStream) {
            val names = new HashSet<>(PREDEFINED_BASECLASS);

            val queue = new ArrayDeque<>(PREDEFINED_BASECLASS);
            val toSubClasses = ArrayListMultimap.<String, String>create();
            dataStream
                .map(AccessClassData::new)
                .forEach(access -> toSubClasses.put(access.parentClassName(), access.className()));
            while (!queue.isEmpty()) {
                val parent = queue.pop();
                names.add(parent);
                val subClasses = toSubClasses.get(parent);
                if (subClasses != null) {
                    queue.addAll(subClasses);
                }
            }

            ProbeJS.LOGGER.debug("ForgeEventSubclassOnly collected {} class names", names.size());
            return names;
        }
    };

    /**
     * will only be used by {@link BuiltinScanners#EVENTS}
     */
    public static final List<String> PREDEFINED_BASECLASS = CollectUtils.ofList(
        Event.class.getName()
    );
}

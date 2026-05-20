package zzzank.probejs.features.kubejs;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import lombok.val;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.utils.Asser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.IntFunction;

/**
 * @author ZZZank
 */
public class EventJSFilter {
    private final ScriptDump dump;
    private final Multimap<String, String> directlyDenied = ArrayListMultimap.create();
    private final List<BiPredicate<EventGroup, EventHandler>> filters = new ArrayList<>();

    public EventJSFilter(ScriptDump dump) {
        this.dump = dump;
        filters.add((eventGroup, eventHandler) -> {
            val byGroup = directlyDenied.get(eventGroup.name);
            return byGroup.contains(eventHandler.name);
        });
    }

    public void deny(String eventGroupName, String eventHandlerName) {
        directlyDenied.put(eventGroupName, eventHandlerName);
    }

    public void denyCustom(BiPredicate<EventGroup, EventHandler> filter) {
        filters.add(Asser.tNotNull(filter, "filter"));
    }

    public BiPredicate<EventGroup, EventHandler> freeze() {
        if (filters.size() == 1) {
            return filters.get(0);
        }
        val frozen = filters.toArray((IntFunction<BiPredicate<EventGroup, EventHandler>[]>) BiPredicate[]::new);
        return (eventGroup, eventHandler) -> {
            for (val filter : frozen) {
                if (filter.test(eventGroup, eventHandler)) {
                    return true;
                }
            }
            return false;
        };
    }

    public ScriptDump getScriptDump() {
        return dump;
    }
}

package zzzank.probejs.features.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import lombok.val;
import zzzank.probejs.lang.typescript.ScriptDump;
import zzzank.probejs.utils.Asser;
import zzzank.probejs.utils.collect.HashMultiMap;
import zzzank.probejs.utils.collect.MultiMap;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * @author ZZZank
 */
public class EventJSFilter {
    private final ScriptDump dump;
    private final MultiMap<String, String> directlyDenied = new HashMultiMap<>();
    private final List<BiPredicate<EventGroup, EventHandler>> filters = new ArrayList<>();

    public EventJSFilter(ScriptDump dump) {
        this.dump = dump;
        filters.add((eventGroup, eventHandler) -> {
            val byGroup = directlyDenied.get(eventGroup.name);
            return byGroup != null && byGroup.contains(eventHandler.name);
        });
    }

    public void deny(String eventGroupName, String eventHandlerName) {
        directlyDenied.add(eventGroupName, eventHandlerName);
    }

    public void denyCustom(BiPredicate<EventGroup, EventHandler> filter) {
        filters.add(Asser.tNotNull(filter, "filter"));
    }

    public BiPredicate<EventGroup, EventHandler> freeze() {
        if (filters.size() == 1) {
            return filters.get(0);
        }
        val frozen = (BiPredicate<EventGroup, EventHandler>[]) filters.toArray(BiPredicate[]::new);
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

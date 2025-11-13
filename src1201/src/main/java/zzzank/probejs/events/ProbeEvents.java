package zzzank.probejs.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface ProbeEvents {
    EventGroup GROUP = EventGroup.of("ProbeEvents");
    EventHandler ADD_GLOBAL = ProbeEvents.GROUP.client("addGlobal", () -> AddGlobalEventJS.class);
    EventHandler ASSIGN_TYPE = ProbeEvents.GROUP.client("assignType", () -> TypeAssignmentEventJS.class);
    EventHandler MODIFY_DOC = ProbeEvents.GROUP.client("modifyClass", () -> TypingModificationEventJS.class);
    EventHandler SNIPPETS = ProbeEvents.GROUP.client("snippets", () -> SnippetGenerationEventJS.class);
}
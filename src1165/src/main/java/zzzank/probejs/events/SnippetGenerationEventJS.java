package zzzank.probejs.events;

import dev.latvian.kubejs.event.EventJS;
import zzzank.probejs.lang.snippet.Snippet;
import zzzank.probejs.lang.snippet.SnippetDump;

import java.util.function.Consumer;

public class SnippetGenerationEventJS extends EventJS {

    private final SnippetDump dump;

    public SnippetGenerationEventJS(SnippetDump dump) {
        this.dump = dump;
    }

    public void create(String name, Consumer<Snippet> handler) {
        handler.accept(dump.snippet(name));
    }
}

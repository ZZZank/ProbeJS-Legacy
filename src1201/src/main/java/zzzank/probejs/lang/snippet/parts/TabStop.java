package zzzank.probejs.lang.snippet.parts;

import org.jetbrains.annotations.Nullable;

public class TabStop extends Enumerable {

    public final String content;

    public TabStop(@Nullable String content) {
        this.content = content;
    }

    @Override
    public String format() {
        if (content == null) return String.format("$%d", enumeration);
        return String.format("${%d:%s}", enumeration, content.replace("$", "\\$"));
    }
}

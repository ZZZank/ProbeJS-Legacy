package zzzank.probejs.lang.snippet.parts;

import java.util.Collection;
import java.util.stream.Collectors;

public class Choice extends Enumerable {
    public final Collection<String> choices;

    public Choice(Collection<String> choices) {
        this.choices = choices;
    }

    @Override
    public String format() {
        String choiceString = choices.stream().map(s -> s.replace(",", "\\,")).collect(Collectors.joining(","));
        return String.format("${%d|%s|}",enumeration, choiceString);
    }
}

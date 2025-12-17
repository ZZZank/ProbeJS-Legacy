package zzzank.probejs.lang.typescript.refer;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.val;
import zzzank.probejs.ProbeJS;

import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@EqualsAndHashCode
@ToString
public final class Reference {
    public final ImportInfo info;
    public final String deduped;

    /**
     * @return the string representing the actual import statement from this import info,
     * in 'import { ... } from ...' format
     */
    public String getImportStatement() {
        val original = info.path.getSimpleName();

        final Function<ImportType, String> nameMapper = original.equals(deduped)
            ? type -> type.fmt(original)
            : type -> String.format("%s as %s", type.fmt(original), type.fmt(deduped));

        val names = info.getTypes()
            .map(nameMapper)
            .collect(Collectors.joining(", "));

        // Underscores can be recognized by using a global export
        return String.format(
            "import { %s } from %s",
            names,
            ProbeJS.GSON.toJson(info.path.getFirstValidPath())
        );
    }

    public String getOriginalName() {
        return info.path.getSimpleName();
    }
}

package zzzank.probejs.features.parchment.data;

import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author ZZZank
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StringIndexer {

    @EqualsAndHashCode.Include
    private final List<String> byIndex = new ArrayList<>();
    private final Map<String, LazyInt> byName = new HashMap<>();
    private Map<String, Integer> built;

    public Number addOrGetIndex(String string) {
        if (isFrozen()) {
            return built.get(string);
        }
        return byName.computeIfAbsent(string, s -> {
            byIndex.add(s);
            return new LazyInt(() -> getIndex(s));
        });
    }

    public Integer getIndex(String string) {
        if (!isFrozen()) {
            throw new IllegalStateException("not frozen");
        }
        return built.get(string);
    }

    public String getValue(int index) {
        return byIndex.get(index);
    }

    public List<String> viewIndexed() {
        return Collections.unmodifiableList(byIndex);
    }

    public List<String> buildDiffs() {
        if (!isFrozen()) {
            throw new IllegalStateException("not frozen");
        }
        var result = new ArrayList<String>(byIndex.size());

        var base = Diffable.of("", '/');
        for (var string : byIndex) {
            var path = Diffable.of(string, base.splitBy);
            result.add(base.toDiff(path));
            base = path;
        }

        return result;
    }

    public boolean isFrozen() {
        return built != null;
    }

    public void freeze() {
        if (isFrozen()) {
            throw new IllegalStateException("already frozen");
        }
        var toSort = byIndex;
        toSort.sort(null);
        built = IntStream.range(0, toSort.size())
            .boxed()
            .collect(Collectors.toMap(toSort::get, Function.identity()));
    }
}

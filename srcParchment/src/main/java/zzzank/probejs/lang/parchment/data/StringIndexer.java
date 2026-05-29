package zzzank.probejs.lang.parchment.data;

import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * @author ZZZank
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class StringIndexer {

    @EqualsAndHashCode.Include
    private final List<String> byIndex = new ArrayList<>();
    private final Map<String, Integer> byName = new HashMap<>();

    public int addOrGetIndex(String string) {
        return byName.computeIfAbsent(
            string, (str) -> {
                var result = byIndex.size();
                byIndex.add(str);
                return result;
            }
        );
    }

    public int getIndex(String string) {
        return byName.getOrDefault(string, -1);
    }

    public String getValue(int index) {
        return byIndex.get(index);
    }

    public List<String> viewIndexed() {
        return Collections.unmodifiableList(byIndex);
    }

    public List<String> buildDiffs() {
        var result = new ArrayList<String>(byIndex.size());

        var baseDiff = PrefixDiff.EMPTY;
        for (var string : byIndex) {
            var diff = baseDiff.toDiff(string);
            result.add(diff.toString());
            baseDiff = diff;
        }

        return result;
    }
}

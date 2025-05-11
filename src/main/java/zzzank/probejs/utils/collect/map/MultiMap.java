package zzzank.probejs.utils.collect.map;

import zzzank.probejs.utils.CollectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author ZZZank
 */
public interface MultiMap<K, E> extends Map<K, List<E>> {

    Function NEW_ARRAYLIST = CollectUtils.ignoreInput(ArrayList::new);

    default List<E> getOrEmpty(Object key) {
        return getOrDefault(key, Collections.emptyList());
    }

    /**
     * @return the list to which the {@code element} is added to
     */
    default List<E> add(K key, E element) {
        final List<E> list = computeIfAbsent(key, NEW_ARRAYLIST);
        list.add(element);
        return list;
    }
}

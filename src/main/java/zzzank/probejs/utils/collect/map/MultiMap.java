package zzzank.probejs.utils.collect.map;

import lombok.val;
import zzzank.probejs.utils.CollectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author ZZZank
 */
public interface MultiMap<K, E> extends Map<K, List<E>> {

    default List<E> getOrEmpty(Object key) {
        return getOrDefault(key, Collections.emptyList());
    }

    /**
     * @return the list to which the {@code element} is added to
     */
    default List<E> add(K key, E element) {
        val list = computeIfAbsent(key, CollectUtils.ignoreInput(ArrayList::new));
        list.add(element);
        return list;
    }
}

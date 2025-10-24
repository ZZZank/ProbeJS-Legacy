package zzzank.probejs.utils;

import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * utils for collections
 *
 * @author ZZZank
 */
public interface CollectUtils {

    @SafeVarargs
    static <T> List<T> ofList(T... elements) {
        val list = new ArrayList<T>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }

    static <T> List<T> ofList(Stream<T> stream) {
        return stream.collect(Collectors.toCollection(ArrayList::new));
    }

    static <K, V> AbstractMap.SimpleImmutableEntry<K, V> ofEntry(K key, V value) {
        return new AbstractMap.SimpleImmutableEntry<>(key, value);
    }

    static <I, O> List<O> mapToList(Collection<I> collection, Function<I, O> mapper) {
        val l = new ArrayList<O>(collection.size());
        for (I i : collection) {
            l.add(mapper.apply(i));
        }
        return l;
    }

    static <I, O> O[] mapToArray(I[] input, Function<I, O> mapper, IntFunction<O[]> generator) {
        val len = input.length;
        val got = generator.apply(len);
        for (int i = 0; i < len; i++) {
            got[i] = mapper.apply(input[i]);
        }
        return got;
    }

    static <I, O> List<O> mapToList(I[] collection, Function<I, O> mapper) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(mapper);
        val l = new ArrayList<O>(collection.length);
        for (I i : collection) {
            l.add(mapper.apply(i));
        }
        return l;
    }

    static <I, O> Function<I, O> ignoreInput(Supplier<O> supplier) {
        Asser.tNotNull(supplier, "supplier");
        return input -> supplier.get();
    }

    static int calcMapExpectedSize(int elementCount) {
        return calcMapExpectedSize(elementCount, 0.75F);
    }

    static int calcMapExpectedSize(int elementCount, float loadFactor) {
        return (int) Math.ceil(elementCount / loadFactor);
    }

    static <K, V> HashMap<K, V> ofSizedMap(int expectedSize) {
        return new HashMap<>(calcMapExpectedSize(expectedSize));
    }

    @Nullable
    static <T> T anyIn(Iterable<T> iterable) {
        val iterator = iterable.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    @Nullable
    static <T> T anyIn(Stream<T> stream) {
        return stream.findAny().orElse(null);
    }

    static <T> Iterator<T> enumToItr(Enumeration<T> enumeration) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return enumeration.hasMoreElements();
            }

            @Override
            public T next() {
                return enumeration.nextElement();
            }
        };
    }

    static <E> Set<E> identityHashSet() {
        return Collections.newSetFromMap(new IdentityHashMap<>());
    }

    static <E> Set<E> identityHashSet(int expectedMaxSize) {
        return Collections.newSetFromMap(new IdentityHashMap<>(expectedMaxSize));
    }

    static <T> Iterable<T> iterate(Enumeration<T> enumeration) {
        return () -> enumToItr(enumeration);
    }

    static <T> Iterable<T> iterate(Stream<T> stream) {
        return stream::iterator;
    }
}

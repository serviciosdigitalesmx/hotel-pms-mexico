package com.hotelpms.frontdesk.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Defensive immutable copies for collection-valued request and response records. */
public final class ImmutableCopies {
    private ImmutableCopies() { }

    /**
     * Copies a list while preserving null list and element values for normal validation handling.
     *
     * @param <T> list element type
     * @param source input list
     * @return an unmodifiable copy, or null when the input is null
     */
    public static <T> List<T> list(final List<T> source) {
        return source == null
                ? null
                : Collections.unmodifiableList(new ArrayList<>(source));
    }

    /**
     * Copies a map and each list value for immutable nested recipient collections.
     *
     * @param <K> map key type
     * @param <V> list element type
     * @param source input map
     * @return an unmodifiable nested copy, or null when the input is null
     */
    public static <K, V> Map<K, List<V>> mapOfLists(final Map<K, List<V>> source) {
        if (source == null) {
            return null;
        }
        final Map<K, List<V>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, list(value)));
        return Collections.unmodifiableMap(copy);
    }
}

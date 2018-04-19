package com.ggface.dovvv.classes;

import android.os.Parcel;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for different actions for collections.
 *
 * @author Ivan Novikov on 2017-12-07.
 */
public final class CollectionUtils {

    /**
     * Wraps the {@param list}.
     *
     * @param list source list.
     * @return new list. If {@param list} equals null, it returns empty list.
     */
    @NonNull
    public static <T> List<T> wrapListNonNull(List<T> list) {
        List<T> result = new ArrayList<>();
        if (list != null) {
            result.addAll(list);
        }
        return result;
    }

    /**
     * Wrap some Map
     *
     * @param map source Map
     * @return new {@code HashMap}. If {@param map} equals null, returns empty {@code HashMap}
     */
    @NonNull
    public static <K, V> Map<K, V> wrapMapNonNull(Map<K, V> map) {
        Map<K, V> result = new HashMap<>();
        if (map != null) {
            result.putAll(map);
        }
        return result;
    }

    public static <T> List<T> readArrayList(Parcel parcel, ClassLoader loader) {
        List<T> list = new ArrayList<>();
        parcel.readList(list, loader);
        return list;
    }
}
package ron.thewizard.roleplayextras.utils;

import ron.thewizard.roleplayextras.RoleplayExtras;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class CollectionUtil {

    public static <E> Set<E> newConcurrentHashSet() {
        return newConcurrentHashSet(16);
    }

    public static <E> Set<E> newConcurrentHashSet(int initialSize) {
        return Collections.newSetFromMap(new ConcurrentHashMap<>(initialSize));
    }

    public static <E> Set<E> newConcurrentHashSet(Collection<E> collection) {
        Set<E> newSet = newConcurrentHashSet(collection.size());
        newSet.addAll(collection);
        return newSet;
    }

    public static <T> T getRandomElement(List<T> list) {
        return list.get(RoleplayExtras.getRandom().nextInt(list.size()));
    }
}

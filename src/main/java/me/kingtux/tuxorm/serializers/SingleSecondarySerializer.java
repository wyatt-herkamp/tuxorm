package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Column;

import java.lang.reflect.Field;

/**
 * Player
 */
public interface SingleSecondarySerializer<T, V> extends SecondarySerializer<T> {
    V getSimplifiedValue(T o);

    T buildFromSimplifiedValue(V value);

    Column createColumn(String name);
}

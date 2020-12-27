package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.sql.SQLColumn;


/**
 * Player
 */
public interface SingleSecondarySerializer<T, V> extends SecondarySerializer<T> {
    V getSimplifiedValue(T o);

    T buildFromSimplifiedValue(V value);

    ColumnBuilder createColumn(String name);
}

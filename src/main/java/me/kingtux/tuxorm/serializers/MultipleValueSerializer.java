package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBRow;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * If it has multiple values like lists and maps use this.
 * Warning this defaults the SUbMMS methods
 *
 * @param <T>
 */
public interface MultipleValueSerializer<T> extends MultiSecondarySerializer<T> {

    List<Object> contains(Object o, Table table);



    //The defaults
    default List<Column> getColumns(String after) {
        return Collections.emptyList();
    }


    default Map<Column, Object> getValues(T t, Table table, String s) {
        return Collections.emptyMap();
    }


    default T minorBuild(DBRow dbRows, String after) {
        return null;
    }


}

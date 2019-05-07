package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBRow;

import java.util.List;
import java.util.Map;

public interface SubMSSCompatible<T> extends MultiSecondarySerializer {

    /**
     * These columns are used for other MultiSecondarySerializer.
     *
     * @return
     */
    List<Column> getColumns(String after);

    default List<Column> getColumns() {
        return getColumns("");
    }

    Map<Column, Object> getValues(T t, Table table, String s);

    default Map<Column, Object> getValues(T t, Table table) {
        return getValues(t, table, "");
    }

    T minorBuild(DBRow dbRows, String after);

    default T minorBuild(DBRow dbRows) {
        return minorBuild(dbRows, "");
    }
}

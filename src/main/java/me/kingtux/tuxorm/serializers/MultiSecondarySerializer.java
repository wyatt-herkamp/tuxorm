package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Locations(Bukkit), Lists, Maps
 */
public interface MultiSecondarySerializer<T> extends SecondarySerializer<T> {

    void insert(T t, Table table, Object parentID, Field field);

    T build(DBResult dbResult, Field field);

    Table createTable(String name, Field field, DataType parentDataType);


    default void delete(Object parentID, Field field, Table table){
        table.delete(getConnection().getBuilder().createWhere().start(TOUtils.PARENT_ID_NAME, TOUtils.simplifyObject(parentID)));
    }

    default WhereStatement where(T o, Table table) {
        WhereStatement where = getConnection().getBuilder().createWhere();
        Map<Column, Object> map = getValues(o, table);
        int i = 0;
        for (Map.Entry<Column, Object> value : map.entrySet()) {
            if (i == 0) {
                where.start(value.getKey().getName(), value.getValue());
            } else {
                where.AND(value.getKey().getName(), value.getValue());
            }
            i++;
        }
        return where;
    }


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


    TOConnection getConnection();
}

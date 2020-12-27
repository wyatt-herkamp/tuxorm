package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.response.DBRow;
import me.kingtux.tuxjsql.core.response.DBSelect;
import me.kingtux.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxjsql.core.sql.SQLDataType;
import me.kingtux.tuxjsql.core.sql.SQLTable;

import me.kingtux.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.annotations.DataType;

import javax.swing.table.TableStringConverter;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Locations(Bukkit), Lists, Maps
 */
public interface MultiSecondarySerializer<T> extends SecondarySerializer<T> {

    void insert(T t, SQLTable table, Object parentID, Field field);

    T build(DBSelect dbResult, Field field);

    SQLTable createTable(String name, Field field, SQLDataType parentDataType);


    default void delete(Object parentID, Field field, SQLTable table) {
        try {
            table.delete().where().start(TOUtils.PARENT_ID_NAME, TOUtils.simplifyObject(parentID)).and().execute().complete();
        } catch (InterruptedException e) {
            TOConnection.logger.error("Unable to get value",e);
        }
    }

    default WhereStatement where(T o, SQLTable table) {
        WhereStatement where = getConnection().getBuilder().createWhere();
        Map<SQLColumn, Object> map = getValues(o, table);
        int i = 0;
        for (Map.Entry<SQLColumn, Object> value : map.entrySet()) {
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
     * @param after What all column names must have after it.
     * @return a list of Columns
     */
    List<ColumnBuilder> getColumns(String after);

    default List<ColumnBuilder> getColumns() {
        return getColumns("");
    }

    Map<SQLColumn, Object> getValues(T t, SQLTable table, String s);

    default Map<SQLColumn, Object> getValues(T t, SQLTable table) {
        return getValues(t, table, "");
    }

    T minorBuild(DBRow dbRows, String after);

    default T minorBuild(DBRow dbRows) {
        return minorBuild(dbRows, "");
    }


    TOConnection getConnection();
}

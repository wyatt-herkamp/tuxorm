package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.TOUtils;

import java.lang.reflect.Field;

/**
 * Locations, Lists, Maps
 */
public interface MultiSecondarySerializer<T> extends SecondarySerializer<T> {

    void insert(T t, Table table, Object parentID, Field field);

    T fetch(Table table, Object parentID, Field field);

    Table createTable(String name, Field field, DataType parentDataType);

    default void delete(Object parentID, Field field, Table table){
        table.delete(TuxJSQL.getBuilder().createWhere().start(TOUtils.PARENT_ID_NAME, TOUtils.simplifyObject(parentID)));
    }

}

package me.kingtux.tuxorm.toobjects;

import me.kingtux.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxjsql.core.sql.SQLTable;

import java.lang.reflect.Field;
import java.util.Map;

public interface TOObject {


    Class<?> getType();

    void setType(Class<?> type);

    SQLTable getTable();

    void setTable(SQLTable table);

    Map<Field, SQLTable> getOtherObjects();

    void setOtherObjects(Map<Field, SQLTable> otherObjects);

    SQLColumn getColumnForField(Field field);

    Field getFieldForColumnName(String columnName);
}

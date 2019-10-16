package me.kingtux.tuxorm.toobjects;

import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.lang.reflect.Field;
import java.util.Map;

public class SimpleTOObject implements TOObject{
    private Class<?> type;
    //The Main SQLTable
    private SQLTable table;
    //These are toobjects the go to another table.
    private Map<Field, SQLTable> otherObjects;

    public SimpleTOObject(Class<?> type, SQLTable table, Map<Field, SQLTable> otherObjects) {
        this.type = type;
        this.table = table;
        this.otherObjects = otherObjects;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public SQLTable getTable() {
        return table;
    }

    public void setTable(SQLTable table) {
        this.table = table;
    }

    public Map<Field, SQLTable> getOtherObjects() {
        return otherObjects;
    }

    public void setOtherObjects(Map<Field, SQLTable> otherObjects) {
        this.otherObjects = otherObjects;
    }

    public SQLColumn getColumnForField(Field field) {
        return table.getColumn(TOUtils.getColumnNameByField(field));
    }

    public Field getFieldForColumnName(String columnName) {
        for (Field field : type.getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            if (TOUtils.getColumnNameByField(field).equals(columnName)) {
                return field;
            }
        }
        return null;
    }
}

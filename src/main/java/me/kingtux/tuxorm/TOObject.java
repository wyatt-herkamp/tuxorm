package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Table;

import java.lang.reflect.Field;
import java.util.Map;

public class TOObject {
    private Class<?> type;
    //The Main Table
    private Table table;
    //These are objects the go to another table.
    private Map<Field, Table> otherObjects;

    public TOObject(Class<?> type, Table table, Map<Field, Table> otherObjects) {
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

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public Map<Field, Table> getOtherObjects() {
        return otherObjects;
    }

    public void setOtherObjects(Map<Field, Table> otherObjects) {
        this.otherObjects = otherObjects;
    }

    public Column getColumnForField(Field field) {
        return table.getColumnByName(TOUtils.getColumnNameByField(field));
    }

    public Field getFieldForColumnName(String columnName) {
        for (Field field : type.getDeclaredFields()) {
            if (TOUtils.getColumnNameByField(field).equals(columnName)) {
                return field;
            }
        }
        return null;
    }
}

package me.kingtux.tuxorm.internal.implementations;

import dev.tuxjsql.core.response.DBColumnItem;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.serializer.Serializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class ComplexExternalORMField implements ORMField {
    private SQLTable table;
    @Override
    public Serializer getSerializer() {
        return null;
    }

    @Override
    public Field getField() {
        return null;
    }

    @Override
    public String getColumnName() {
        return null;
    }

    @Override
    public Map<SQLColumn, Object> parse(Object object) {
        return null;
    }

    @Override
    public Object parse(List<DBColumnItem> dbColumnItemList) {
        return null;
    }

    @Override
    public void set(Object object, Object instance) {

    }

    @Override
    public Object get(Object instance) {
        return null;
    }

    public SQLTable getTable() {
        return table;
    }

    public void setTable(SQLTable table) {
        this.table = table;
    }
}

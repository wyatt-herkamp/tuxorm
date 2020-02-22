package me.kingtux.tuxorm.internal.implementations;

import dev.tuxjsql.core.response.DBColumnItem;
import dev.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.serializer.Serializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class BasicInternalORMField implements ORMField {
    private Field field;

    @Override
    public Serializer getSerializer() {
        return null;
    }

    @Override
    public Field getField() {
        return field;
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
        try {
            field.set(object, instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object get(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}

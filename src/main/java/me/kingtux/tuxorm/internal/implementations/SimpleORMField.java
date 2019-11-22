package me.kingtux.tuxorm.internal.implementations;

import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.internal.ORMFieldType;
import me.kingtux.tuxorm.internal.ORMResult;

import java.lang.reflect.Field;

public class SimpleORMField implements ORMField {
    private Field field;
    private SQLTable sqlTable;
    private ORMFieldType fieldType;
    private TuxORM tuxORM;

    public SimpleORMField(Field field, TuxORM tuxORM) {
        this.field = field;
        this.tuxORM = tuxORM;
        fieldType = ORMFieldType.INTERNAL;
        this.field.setAccessible(true);
    }

    public SimpleORMField(Field field, SQLTable sqlTable, TuxORM tuxORM) {
        this.field = field;
        this.sqlTable = sqlTable;
        this.tuxORM = tuxORM;
        fieldType = ORMFieldType.EXTERNAL;
        this.field.setAccessible(true);
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public SQLTable getTable() {
        return null;
    }

    @Override
    public ORMFieldType fieldType() {
        return fieldType;
    }

    @Override
    public Object getValue(Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            TuxORM.LOGGER.error("Unable to get value", e);
        }
        return null;
    }

    @Override
    public void setValue(Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            TuxORM.LOGGER.error("Unable to set value", e);
        }
    }

    @Override
    public Object insert(Object value) {
        return null;
    }

    @Override
    public void insert(Object value, Object parentKey) {

    }

    @Override
    public Object getFieldResult(ORMResult result) {
        return null;
    }
}

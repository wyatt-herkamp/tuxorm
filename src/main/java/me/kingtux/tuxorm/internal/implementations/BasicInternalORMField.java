package me.kingtux.tuxorm.internal.implementations;

import dev.tuxjsql.core.builders.ColumnBuilder;
import dev.tuxjsql.core.response.DBColumnItem;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLDataType;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.annotations.Column;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.serializer.Serializer;
import me.kingtux.tuxorm.utils.TuxORMUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class BasicInternalORMField implements ORMField {
    private Field field;
    private SQLDataType dataType;

    public BasicInternalORMField(Field field, SQLDataType dataType) {
        this.field = field;
        field.setAccessible(true);
        this.dataType = dataType;
    }

    @Override
    public Serializer getSerializer() {
        return null;
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public String getColumnName() {
        return TuxORMUtils.getFieldName(field);
    }

    @Override
    public Map<SQLColumn, Object> parse(Object object) {
        return null;
    }

    @Override
    public Object parse(List<DBColumnItem> dbColumnItemList) {
        return null;
    }

    public SQLColumn createColumn(TuxORM tuxORM) {
        Column dbField = field.getAnnotation(Column.class);
        ColumnBuilder builder = tuxORM.getTuxJSQL().createColumn().
                setDataType(dataType).
                name(getColumnName());
        if (dbField.autoIncrement()) builder.autoIncrement();
        if (dbField.primary()) builder.primaryKey();
        if (dbField.unique()) builder.unique();
        return builder.build();
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

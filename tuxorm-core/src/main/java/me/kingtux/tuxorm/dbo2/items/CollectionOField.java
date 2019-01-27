package me.kingtux.tuxorm.dbo2.items;

import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.ObjectField;

import java.lang.reflect.Field;

/**
 * This is not going to hold much. It basically means that its there is a collection with this object
 */
public class CollectionOField implements DBField, ObjectField {
    private Class<?> clazz;
    private Field field;

    public CollectionOField(Field field) {
        this.field = field;
        clazz = field.getDeclaringClass();
    }

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public Field getField() {
        return field;
    }
}

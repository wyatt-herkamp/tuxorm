package me.kingtux.tuxorm.dbo2.items;

import me.kingtux.tuxorm.ORMUtils;
import me.kingtux.tuxorm.annotations.DatabaseField;
import me.kingtux.tuxorm.annotations.ForeignField;
import me.kingtux.tuxorm.annotations.Id;
import me.kingtux.tuxorm.annotations.ORMCollection;
import me.kingtux.tuxorm.datatypes.CommonDataTypes;
import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.DatabaseRules;
import me.kingtux.tuxorm.dbo2.Nameable;
import me.kingtux.tuxorm.dbo2.ObjectField;

import java.lang.reflect.Field;

public class ObjectColumnField implements ObjectField, DBField, DatabaseRules, Nameable {

    private Class<?> clazz;
    private Field field;

    private Class<?> dataType;

    private boolean primaryKey;

    public ObjectColumnField(Field field) {
        this.field = field;
        clazz = field.getDeclaringClass();

        dataType = field.getType();


        //It is a ForeignField
        if (field.isAnnotationPresent(ForeignField.class)) {
            doesThisRelate = true;
            otherObject = dataType;
            dataType = ORMUtils.getPrimaryKey(otherObject);
        }
        if (field.isAnnotationPresent(Id.class)) {
            primaryKey = true;
        }


    }

    //OtherObject
    private boolean doesThisRelate = false;
    private Class<?> otherObject = null;

    @Override
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public Field getField() {
        return field;
    }

    public boolean doesThisRelateToAnotherObject() {
        return doesThisRelate;

    }

    public Class<?> otherObject() {
        return otherObject;
    }


    public String getName() {
        if (field.getAnnotation(DatabaseField.class).name().equals("")) {
            return field.getName().toLowerCase();
        }
        return field.getAnnotation(DatabaseField.class).name();
    }

    @Override
    public boolean isPrimary() {
        return primaryKey;
    }

    @Override
    public boolean autoIncrement() {
        return field.getAnnotation(ORMCollection.class).autoIncrement();
    }

    @Override
    public boolean isNullable() {
        return field.getAnnotation(DatabaseField.class).nullable();
    }

    @Override
    public CommonDataTypes getDataType() {
        if (doesThisRelateToAnotherObject()) {
            return CommonDataTypes.INT;
        }
        return CommonDataTypes.getByType(dataType);
    }
}

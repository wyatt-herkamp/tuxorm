package me.kingtux.tuxorm;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.toobjects.TOObject;

import java.lang.reflect.InvocationTargetException;

public class TOValidator {
    private TOValidator() {

    }

    public static void validateClass(Class<?> tableClass) {
        if (tableClass == null) {
            throw new IllegalArgumentException("the provided class is null");
        }
        if (tableClass.getAnnotation(DBTable.class) == null) {
            throw new IllegalArgumentException(tableClass.getName() + " is missing required @DBTable");
        }
        try {
            tableClass.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new TOException("Unable to find public constructor!");
        }
    }

    public static Object validateUpdate(Object o, TOObject object, DefaultSerializer serializer) {
        if (o == null || object == null) {
            throw new NullPointerException("The Values provided for update are null");
        }
        Object primaryKeyValue = serializer.getPrimaryKey(o);
        if (primaryKeyValue == null) {
            throw new TOException("Hey unable to locate a primarykey for " + o.getClass().getSimpleName());
        }
        return primaryKeyValue;
    }

    public static void validateCreate(Object value, TOObject toObject) {
        if(value == null){
            throw new NullPointerException("Cant create with null");
        }
    }
}

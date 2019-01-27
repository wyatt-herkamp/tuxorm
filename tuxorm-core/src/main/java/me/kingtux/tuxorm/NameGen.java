package me.kingtux.tuxorm;

import me.kingtux.tuxorm.annotations.DatabaseTable;

import java.lang.reflect.Field;

public class NameGen {
    public static String getTableName(Class<?> clazz) {
        DatabaseTable db = clazz.getAnnotation(DatabaseTable.class);
        if (db == null) return clazz.getSimpleName().toLowerCase();
        if (db.name().equals("")) return clazz.getSimpleName();
        return db.name();
    }

    public static String getIdName(Class<?> clazz) {
        return clazz.getSimpleName().toLowerCase() + "_id";
    }

    public static String getCollectionFieldTableName(Field field, Class<?> clazz) {
        return field.getName().toLowerCase() + "_" + getTableName(clazz);
    }
}

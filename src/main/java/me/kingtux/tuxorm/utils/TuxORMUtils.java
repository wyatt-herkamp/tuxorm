package me.kingtux.tuxorm.utils;

import me.kingtux.tuxorm.annotations.Column;
import me.kingtux.tuxorm.annotations.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TuxORMUtils {
    /**
     * this method will collect all fields from parent classes.
     *
     * @param clazz the class to check
     * @return a list of fields from class and parent classes
     */
    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static String getTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table.value().equals("")) return clazz.getSimpleName().toLowerCase();
        return table.value();
    }

    public static String getFieldName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column.name().equals("")) return field.getName().toLowerCase();
        return column.name();
    }
}

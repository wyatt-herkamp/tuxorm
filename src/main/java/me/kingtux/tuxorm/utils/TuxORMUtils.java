package me.kingtux.tuxorm.utils;

import me.kingtux.tuxorm.annotations.DatabateTable;

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

    public String getTableName(Class<?> clazz) {
        DatabateTable table = clazz.getAnnotation(DatabateTable.class);
        if (table.value().equals("")) return clazz.getSimpleName().toLowerCase();
        return table.value();
    }
}

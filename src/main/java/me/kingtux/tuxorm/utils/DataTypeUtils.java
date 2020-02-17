package me.kingtux.tuxorm.utils;


import dev.tuxjsql.basic.sql.BasicDataTypes;
import dev.tuxjsql.core.sql.SQLDataType;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.annotations.DataType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataTypeUtils {

    private static Map<String, Class<?>> registeredDataTypeAnnotations = new HashMap<>();

    public static void registerDataTypeAnnotation(String name, Class<?> clazz) {
        registeredDataTypeAnnotations.put(name, clazz);
    }

    public static SQLDataType getDataType(Field field, String name) {
        SQLDataType dataType = null;
        try {
            dataType = getPreferredDataType(field, name);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            TuxORM.LOGGER.error("Unable to get DataType for Field", e);
        }
        if (dataType != null) return dataType;
        return getLogicalDataType(field);
    }

    private static SQLDataType getPreferredDataType(Field field, String name) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class type = registeredDataTypeAnnotations.getOrDefault(name, DataType.class);
        Object annotation = field.getAnnotation(type);
        if (annotation == null) return null;
        Method method = type.getMethod("dataType");
        return (SQLDataType) method.invoke(annotation);
    }
    private static SQLDataType getLogicalDataType(Field field) {
        for (LogicalDataTypes logicalDataTypes : LogicalDataTypes.values()) {
            if (logicalDataTypes.supportedDataTypes.contains(field.getType())) {
                return logicalDataTypes.dataType;
            }
        }
        return null;
    }

    private enum LogicalDataTypes {
        INT(BasicDataTypes.INTEGER, List.of(int.class, Integer.class));

        private SQLDataType dataType;

        private List<Class<?>> supportedDataTypes;

        LogicalDataTypes(BasicDataTypes type, List<Class<?>> classes) {
            this.dataType = type;
            supportedDataTypes = classes;
        }
    }
}

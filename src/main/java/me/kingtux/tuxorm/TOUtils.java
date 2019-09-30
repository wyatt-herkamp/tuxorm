package me.kingtux.tuxorm;

import dev.tuxjsql.basic.sql.BasicDataTypes;
import dev.tuxjsql.core.builders.SQLBuilder;
import dev.tuxjsql.core.builders.TableBuilder;
import dev.tuxjsql.core.response.DBRow;
import dev.tuxjsql.core.response.DBSelect;
import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.SQLTable;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;
import me.kingtux.tuxorm.exceptions.MissingValueException;
import me.kingtux.tuxorm.exceptions.UnableToLocateException;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class TOUtils {
    public static final String PARENT_ID_NAME = "parent";
    private static List<Class<?>> basicTypes = Arrays.asList(long.class, Long.class, String.class, int.class, Integer.class);
    private static List<Class<?>> semiBasicTypes = Arrays.asList(UUID.class, Boolean.class, boolean.class, char[].class, Enum.class);

    private TOUtils() {
    }

    public static boolean isBasic(Class<?> e) {
        for (Class<?> c : basicTypes) {
            if (c.isAssignableFrom(e)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSemiBasic(Class<?> e) {
        for (Class<?> c : semiBasicTypes) {
            if (c.isAssignableFrom(e)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAnyTypeBasic(Class<?> e) {
        return isBasic(e) || isSemiBasic(e);
    }

    public static SQLDataType getColumnType(Class<?> type) {
        if (type == String.class) {
            return BasicDataTypes.TEXT;
        } else if (type == int.class || type == Integer.class) {
            return BasicDataTypes.INTEGER;
        } else if (type == double.class || type == Double.class) {
            return BasicDataTypes.REAL;
        } else if (type == long.class || type == Long.class) {
            return BasicDataTypes.INTEGER;
        } else if (type == boolean.class || type == Boolean.class) {
            return BasicDataTypes.TEXT;
        }
        return BasicDataTypes.TEXT;
    }

    //These Two Methods goal is to turn kinda simple types to simple types
    public static Object simplifyObject(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return ((boolean) o) ? 1 : 0;
        } else if (o instanceof UUID) {
            return o.toString();
        } else if (o.getClass().isEnum()) {
            return ((Enum) o).name();
        } else if (o.getClass() == char[].class) {
            return String.valueOf(o);
        }
        return o;
    }

    public static Class<?> getFirstTypeParam(Field field) {
        return getTypeParamAt(field, 0);
    }


    public static Class<?> getTypeParamAt(Field field, int i) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) field.getGenericType();
            try {
                String clazz = ptype.getActualTypeArguments()[i].
                        toString().replace("class ", "").replace("interface ", "");
                if (TOConnection.logger.isDebugEnabled())
                    TOConnection.logger.debug(String.format("%s Type is %s", field.getDeclaringClass().getName(), clazz));
                return field.getDeclaringClass().getClassLoader().loadClass(clazz);
            } catch (ClassNotFoundException e) {
                TOConnection.logger.error("Unable to locate class", e);
                return null;
            }
        } else {
            return null;
        }
    }

    public static Object quickInsert(Object value, TOConnection connection) {
        Dao<Object, Object> dao = connection.createDao(value);
        return dao.create(value);
    }

    public static <T> Object quickGet(Class<T> type, Object id, TOConnection connection) {

        Dao<T, Object> dao = connection.createDao(type);
        return dao.findByID(id).orElseThrow(()->new UnableToLocateException("Unable to get sub value"));
    }

    public static TableBuilder basicTable(SQLBuilder builder, String name, SQLDataType parentDataType) {
        TableBuilder tableBuilder = builder.createTable().setName(name);
        tableBuilder.addColumn(builder.createColumn().name("id").primaryKey().autoIncrement().setDataType(BasicDataTypes.INTEGER));
        tableBuilder.addColumn(builder.createColumn().name(PARENT_ID_NAME).setDataType(parentDataType));
        return tableBuilder;
    }

    @SuppressWarnings("unchecked")
    public static Object rebuildObject(Class<?> type, Object o) {
        if (type == Boolean.class || type == boolean.class) {
            return (Integer.parseInt(o.toString())) == 1;
        } else if (type == UUID.class) {
            return UUID.fromString(((String) o));
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, ((String) o));
        } else if (type == char[].class) {
            return ((String) o).toCharArray();
        }
        return o;
    }

    public static Class<?> simpleClass(Class<?> type) {
        if (type == null) return null;
        if (type == Boolean.class || type == boolean.class) {
            return int.class;
        } else if (type == UUID.class) {
            return String.class;
        } else if (type.isEnum()) {
            return String.class;
        } else if (type == char[].class) {
            return String.class;
        }
        return type;
    }

    public static String getColumnNameByField(Field field) {
        TableColumn column = field.getAnnotation(TableColumn.class);
        return column.name().isEmpty() ? field.getName().toLowerCase() : column.name().toLowerCase();

    }

    public static String getClassName(Class<?> type) {
        DBTable table = type.getAnnotation(DBTable.class);
        if (table == null) return null;
        return table.name().isEmpty() ? type.getSimpleName().toLowerCase() : table.name();
    }

    public static boolean isPrimitveNull(Object pkey) {
        return pkey.equals(0) || pkey.equals(0L) || pkey.equals(0.0);
    }


    public static List<Object> contains(Object o, SQLTable table, TOConnection connection, String key) {

        DBSelect result = null;
        try {
            if (isAnyTypeBasic(o.getClass())) {
                result = table.select().where().start(key, o).and().execute().complete();

            } else {
                SecondarySerializer ss = connection.getSecondarySerializer(o.getClass());
                if (ss == null) {
                    result = table.select().where().start(key, connection.getPrimaryValue(o)).and().execute().complete();
                } else {
                    if (ss instanceof SingleSecondarySerializer) {
                        result = table.select().where().start(key, ((SingleSecondarySerializer) ss).getSimplifiedValue(o)).and().execute().complete();
                    } else if (ss instanceof MultiSecondarySerializer) {
                        MultiSecondarySerializer mssCompatible = (MultiSecondarySerializer) ss;
                        result = table.select().where(mssCompatible.where(o, table)).execute().complete();
                    }
                }
            }
        }catch (InterruptedException e){
            TOConnection.logger.error("Unable to get value",e);
            Thread.currentThread().interrupt();
        }
        if (result == null) return Collections.emptyList();

        return ids(result, o);
    }

    public static List<Object> ids(DBSelect result, Object o) {
        List<Object> objects = new ArrayList<>();
        for (DBRow row : result) {
            objects.add(rebuildObject(o.getClass(), row.getColumn(PARENT_ID_NAME).orElseThrow(()-> new MissingValueException("Unable to locate "+ PARENT_ID_NAME)).getAsObject()));
        }

        return objects;
    }
}

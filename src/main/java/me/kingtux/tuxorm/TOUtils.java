package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;
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

    public static DataType getColumnType(Class<?> type) {
        if (type == String.class) {
            return CommonDataTypes.TEXT;
        } else if (type == int.class || type == Integer.class) {
            return CommonDataTypes.INT;
        } else if (type == double.class || type == Double.class) {
            return CommonDataTypes.DOUBLE;
        } else if (type == long.class || type == Long.class) {
            return CommonDataTypes.BIGINT;
        } else if (type == boolean.class || type == Boolean.class) {
            return CommonDataTypes.TEXT;
        }
        return CommonDataTypes.TEXT;
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
                return Class.forName(ptype.getActualTypeArguments()[i].
                        toString().replace("class ", "").replace("interface ", ""));
            } catch (ClassNotFoundException e) {
                TOConnection.logger.error("Unable to locate class",e);
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
        return dao.findByID(id);
    }

    public static TableBuilder basicTable(SQLBuilder builder, String name, DataType parentDataType) {
        TableBuilder tableBuilder = builder.createTable().name(name);
        tableBuilder.addColumn(builder.createColumn().name("id").primary(true).autoIncrement(true).type(CommonDataTypes.INT).build());
        tableBuilder.addColumn(builder.createColumn().name(PARENT_ID_NAME).type(parentDataType).build());
        return tableBuilder;
    }
    @SuppressWarnings("unchecked")
    public static Object rebuildObject(Class<?> type, Object o) {
        if (type == Boolean.class || type == boolean.class) {
            return ((int) o) == 1;
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
        return column.name().isEmpty() ? field.getName().toLowerCase() : column.name();

    }

    public static String getClassName(Class<?> type) {
        DBTable table = type.getAnnotation(DBTable.class);
        if (table == null) return null;
        return table.name().isEmpty() ? type.getSimpleName().toLowerCase() : table.name();
    }

    public static boolean isPrimitveNull(Object pkey) {
        return pkey.equals(0) || pkey.equals(0L) || pkey.equals(0.0);
    }




    public static List<Object> contains(Object o, Table table, TOConnection connection, String key) {
        List<Object> objects = new ArrayList<>();
        DBResult result = null;
        if (isAnyTypeBasic(o.getClass())) {
            result = table.select(connection.getBuilder().createWhere().start(key, o));

        } else {
            SecondarySerializer ss = connection.getSecondarySerializer(o.getClass());
            if (ss == null) {
                result = table.select(connection.getBuilder().createWhere().start(key, connection.getPrimaryValue(o)));
            } else {
                if (ss instanceof SingleSecondarySerializer) {
                    result = table.select(connection.getBuilder().createWhere().start(key, ((SingleSecondarySerializer) ss).getSimplifiedValue(o)));
                } else if (ss instanceof MultiSecondarySerializer) {
                    MultiSecondarySerializer mssCompatible = (MultiSecondarySerializer) ss;
                    WhereStatement where = connection.getBuilder().createWhere();
                    Map<Column, Object> map = mssCompatible.getValues(o, table);
                    int i = 0;
                    for (Map.Entry<Column, Object> value : map.entrySet()) {
                        if (i == 0) {
                            where.start(value.getKey().getName(), value.getValue());
                        } else {
                            where.AND(value.getKey().getName(), value.getValue());
                        }
                        i++;
                    }
                    result = table.select(where);
                }
            }
        }
        if (result == null) return Collections.emptyList();
        for (DBRow row : result) {
            objects.add(rebuildObject(o.getClass(), row.getRowItem(PARENT_ID_NAME).getAsObject()));
        }
        return objects;
    }
}

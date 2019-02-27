package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.*;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("All")
public class TOUtils {
    private static List<Class<?>> basicTypes = Arrays.asList(long.class, Long.class, String.class, Boolean.class, boolean.class, int.class, Integer.class);
    private static List<Class<?>> incompatibleTypes = Arrays.asList(Map.class);
    public static boolean isBasic(Class<?> e) {
        return basicTypes.contains(e);
    }

    public static String getTableName(Class<?> clazz) {
        DBTable table = clazz.getAnnotation(DBTable.class);
        if (table == null) return null;
        return table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
    }

    public static Object cleanObject(Object o) {
        if (o instanceof Boolean) {
            return o.toString();
        } else if (o.getClass().isEnum()) {
            return ((Enum) o).name();
        } else if (o.getClass().isAssignableFrom(File.class)) {
            return ((File) o).getAbsolutePath();
        }
        return o;
    }

    public static Object rebuiltObject(Class<?> type, Object value) {
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean((String) value);
        } else if (type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) type, ((String) value));
        } else if (type.isAssignableFrom(File.class)) {
            return new File((String) value);
        }
        return value;
    }
    public static Column createColumn(Field field) {
        TableColumn tc = field.getAnnotation(TableColumn.class);
        String name = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
        if (isBasic(field.getType())) {
            return TuxJSQL.getBuilder().createColumn(name, tc.dataType().getColumnType(field.getType()), tc.primary(), tc.nullable(), tc.unique(), tc.autoIncrement());
        } else {
            return TuxJSQL.getBuilder().createColumn(name, CommonDataTypes.INT);
        }
    }

    public static boolean isCompatible(Field field) {
        if (field.getType().isArray()) {
            return false;
        }
        for (Class<?> s : incompatibleTypes) {
            if(field.getType().isAssignableFrom(s)){
                return true;
            }
        }
        return false;
    }

    public static Class<?> getFirstTypeParam(Field field) {
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType ptype = (ParameterizedType) field.getGenericType();
            try {
                return Class.forName(ptype.getActualTypeArguments()[0].toString().replace("class ", ""));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    public static DataType typeFor(Class<?> firstTypeParam) {
        if (firstTypeParam == String.class || firstTypeParam == Boolean.class || firstTypeParam == boolean.class) {
            return CommonDataTypes.TEXT;
        } else {
            return CommonDataTypes.INT;
        }
    }

    public static <T> T buildItem(Class<?> type, T t, ResultSet r, me.kingtux.tuxjsql.core.Table table, ORMConnection connection) {
        List<T> st = buildItems(type, t, r, table, connection);
        if (st == null) return null;
        return st.isEmpty() ? null : st.get(0);
    }

    public static <T> List<T> buildItems(Class<?> type, T t, ResultSet r, me.kingtux.tuxjsql.core.Table table, ORMConnection connection) {
        List<T> items = new ArrayList<>();
        try {
            Column primaryColumn = table.getPrimaryColumn();
            while (r.next()) {
                T st = (T) type.getConstructor().newInstance();
                Object o = r.getObject(primaryColumn.getName());
                for (Field field : type.getDeclaredFields()) {
                    if (field.getAnnotation(TableColumn.class) == null) continue;
                    TableColumn tc = field.getAnnotation(TableColumn.class);
                    field.setAccessible(true);
                    if (isBasic(field.getType())) {
                        Object value = r.getObject(tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name());
                        field.set(st, rebuiltObject(field.getType(), value));
                    } else if (field.getType().isAssignableFrom(List.class)) {
                        field.set(st, isBasic(getFirstTypeParam(field)) ? buildSimpleList(o, connection.getListTable(field), connection) : buildComplexList(o, connection.getListTable(field), field.getType().getConstructor().newInstance(), connection));
                    } else {
                        field.set(st, connection.getValue(field.getType().getConstructor().newInstance(), r.getInt(tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name())));
                    }
                }
                items.add(st);
                //System.out.println("Item Found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    public static List<?> buildSimpleList(Object parentID, Table table, ORMConnection connection) {
        ResultSet set = table.select(TuxJSQL.getBuilder().createWhere().start("parent", parentID));
        List<Object> o = new ArrayList<>();
        try {
            while (set.next()) {
                Dao<Object, Object> dao = connection.createDAO(new Object());
                o.add(dao.findByID(set.getObject("member")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return o;
    }

    public static <T> List<T> buildComplexList(Object parentID, Table table, T t, ORMConnection ormConnection) {
        ResultSet set = table.select(TuxJSQL.getBuilder().createWhere().start("parent", parentID));
        List<T> o = new ArrayList<>();
        try {
            while (set.next()) {
                T item = null;
                    Dao<Object, Object> dao = ormConnection.createDAO(t);
                    item = (T) dao.findByID(set.getObject("member"));
                o.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    public static Field getPrimaryKeyField(Table table, Class<?> c) {
        for (Field field : c.getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            TableColumn tc = field.getAnnotation(TableColumn.class);
            String columnName = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
            if (table.getPrimaryColumn().getName().equalsIgnoreCase(columnName)) {
                return field;
            }
        }
        return null;
    }


    public static Object getPrimaryKeyValue(Table tb, Object o) {

        Object o1 = null;
        Field fi = getPrimaryKeyField(tb, o.getClass());
        if(fi==null) return null;
        fi.setAccessible(true);
        try {
            o1 = fi.get(o);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return o1;
    }

    public static boolean containsFieldWithType(Class<?> c, Class<?> table) {
        for (Field field : c.getDeclaredFields()) {
            if (field.getType() == table) {
                return true;
            }
        }
        return false;
    }
}

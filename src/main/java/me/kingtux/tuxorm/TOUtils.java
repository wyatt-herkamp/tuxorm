package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.*;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("All")
public class TOUtils {

    public static boolean isbasic(Class<?> e) {
        if (e.isPrimitive()) return true;
        return e == String.class;
    }

    public static String getTableName(Class<?> clazz) {
        DBTable table = clazz.getAnnotation(DBTable.class);
        if (table == null) return null;
        return table.name().isEmpty() ? clazz.getSimpleName().toLowerCase() : table.name();
    }

    public static Column createColumn(Field field) {
        TableColumn tc = field.getAnnotation(TableColumn.class);
        String name = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
        if (isbasic(field.getType())) {
            return TuxJSQL.getBuilder().createColumn(name, tc.dataType().getColumnType(field.getType()), tc.primary(), tc.nullable(), tc.unique(), tc.autoIncrement());
        } else {
            return TuxJSQL.getBuilder().createColumn(name, CommonDataTypes.INT);
        }
    }

    public static boolean isCompatible(Field field) {
        if (field.getType().isArray()) {
            return false;
        }
        if (field.getType().isAssignableFrom(Map.class)) {
            return false;
        }
        return true;
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

    public static ColumnType typeFor(Class<?> firstTypeParam) {
        if (firstTypeParam == String.class) {
            return CommonDataTypes.TEXT;
        } else {
            return CommonDataTypes.INT;
        }
    }

    public static <T> T buildItem(Class<?> type, T t, ResultSet r, me.kingtux.tuxjsql.core.Table table, ORMConnection connection) {
        List<T> st = buildItems(type, t, r, table, connection);
        if (st == null) {
            return null;
        }
        return st.isEmpty() ? null : st.get(0);
    }

    public static <T> List<T> buildItems(Class<?> type, T t, ResultSet r, me.kingtux.tuxjsql.core.Table table, ORMConnection connection) {
        List<T> items = new ArrayList<>();
        try {
            T st = (T) type.getConstructor().newInstance();
            Column primaryColumn = table.getPrimaryColumn();
            while (r.next()) {
                Object o = r.getObject(primaryColumn.getName());
                for (Field field : type.getDeclaredFields()) {
                    if (field.getAnnotation(TableColumn.class) == null) continue;
                    TableColumn tc = field.getAnnotation(TableColumn.class);
                    field.setAccessible(true);
                    if (isbasic(field.getType())) {
                        field.set(st, r.getObject(tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name()));
                    } else if (field.getType().isAssignableFrom(List.class)) {
                        field.set(st, buildList(o, connection.getListTable(field), getFirstTypeParam(field).getConstructor().newInstance(), connection));
                    } else {
                        field.set(st, connection.getValue(field.getType().getConstructor().newInstance(), r.getInt(tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name())));
                    }
                }
            }
            System.out.println(items.size() + " " + st.getClass().getName());
            items.add(st);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return items;
    }

    public static <T> List<T> buildList(Object parentID, Table table, T t, ORMConnection ormConnection) {
        ResultSet set = table.select(TuxJSQL.getBuilder().createWhere().start("parent", parentID));
        List<T> o = new ArrayList<>();
        try {
            while (set.next()) {
                T item = null;

                if (isbasic(t.getClass())) {
                    item = (T) set.getObject("member");
                } else {
                    Dao<Object, Object> dao = ormConnection.createDAO(t);
                    item = (T) dao.findByID(set.getObject("member"));
                }
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
}

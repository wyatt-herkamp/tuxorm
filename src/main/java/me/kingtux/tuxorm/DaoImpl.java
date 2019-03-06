package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Builder;
import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("All")
public class DaoImpl<T, ID> implements Dao<T, ID> {
    private ORMConnection connection;
    private Table tb;
    private Class<?> tableClass;
    private Builder sqlBuilder = TuxJSQL.getBuilder();

    DaoImpl(ORMConnection connection, Table table) {
        this.connection = connection;
        this.tb = table;
    }

    DaoImpl(ORMConnection connection, Table tb, Class<?> tableClass) {
        this.connection = connection;
        this.tb = tb;
        this.tableClass = tableClass;
    }

    @Override
    public T findByID(ID id) {
        if(id==null) return null;
        try {
            return (T) TOUtils.buildItem(tableClass, tableClass.getConstructor().newInstance(), tb.select(id), tb, connection);
        } catch (Exception e) {
            throw new TORMException(e);
        }
    }

    @Override
    public void update(T t) {
        Map<Column, Object> items = new HashMap<>();
        List<Field> listFields = new ArrayList<>();
        Object tid = TOUtils.getPrimaryKeyValue(tb, t);
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            TableColumn tc = field.getAnnotation(TableColumn.class);
            String columnName = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
            field.setAccessible(true);
            try {
                if (field.getType().isAssignableFrom(List.class)) {
                    Table listTable = connection.getListTable(field);
                    listTable.delete(sqlBuilder.createWhere().start("parent", tid));
                    listFields.add(field);
                } else if (TOUtils.isBasic(field.getType())) {
                    Object value = field.get(t);
                    items.put(tb.getColumnByName(columnName), value instanceof Boolean ? value.toString() : value);
                } else {
                    Field pf = TOUtils.getPrimaryKeyField(tb, field.getType());
                    pf.setAccessible(true);
                    Object o = pf.get(field.get(t));
                        items.put(tb.getColumnByName(columnName), o == null ? 0 : o);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        tb.update(TOUtils.getPrimaryKeyValue(tb, t), items);

        for (Field field : listFields) {
            try {
                TableColumn tc = field.getAnnotation(TableColumn.class);
                Table listTable = connection.getListTable(field);
                List<?> values = (List<?>) field.get(t);
                if (TOUtils.isBasic(field.getType())) {
                    for (Object o : values) {

                        if (o instanceof Boolean) {
                            listTable.insertAll(tid, o.toString());
                        } else {
                            listTable.insertAll(tid, o);
                        }
                    }
                } else {
                    for (Object o : values) {
                        Field pf = TOUtils.getPrimaryKeyField(tb, o.getClass());
                        //You know sometimes something acts stupid
                        if(pf==null) continue;
                        //End of stupid check
                        pf.setAccessible(true);
                        Object pko = pf.get(o);
                        listTable.insertAll(tid, pko);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void create(T t) {
        Map<Column, Object> items = new HashMap<>();
        List<Field> listFields = new ArrayList<>();
        Object primaryValue = null;
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            TableColumn tc = field.getAnnotation(TableColumn.class);
            String columnName = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
            field.setAccessible(true);
            try {
                if (field.get(t) == null) {
                    continue;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            try {
                if (field.getType().isAssignableFrom(List.class)) {
                    listFields.add(field);
                } else if (TOUtils.isBasic(field.getType())) {
                    Column column = tb.getColumnByName(columnName);
                    if (column.isPrimary()) {
                        if (!column.isAutoIncrement()) {
                            primaryValue = field.get(t);
                        }
                    }
                    if (column.isAutoIncrement()) continue;
                    //Its not something that should be decided by the sql
                    Object value = field.get(t);
                    items.put(column, value instanceof Boolean ? value.toString() : value);
                } else {
                    Field pf = TOUtils.getPrimaryKeyField(tb, field.getType());
                    pf.setAccessible(true);
                    Object o = pf.get(field.get(t));
                    items.put(tb.getColumnByName(columnName), o == null ? 0 : o);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        tb.insert(items);
        if (listFields.isEmpty()) return;
        Object id = primaryValue == null ? tb.max(tb.getPrimaryColumn()) : primaryValue;
        for (Field field : listFields) {
            try {
                TableColumn tc = field.getAnnotation(TableColumn.class);
                Table listTable = connection.getListTable(field);
                List<?> values = (List<?>) field.get(t);
                if (TOUtils.isBasic(TOUtils.getFirstTypeParam(field))) {
                    for (Object o : values) {
                        if (o instanceof Boolean) {
                            listTable.insertAll(id, o.toString());
                        } else {
                            listTable.insertAll(id, o);
                        }
                    }
                } else {
                    for (Object o : values) {
                        Field pf = TOUtils.getPrimaryKeyField(tb, o.getClass());
                        pf.setAccessible(true);
                        Object pko = pf.get(o);
                        listTable.insertAll(id, pko);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<T> fetchAll() {
        try {
            return (List<T>) TOUtils.buildItems(tableClass, tableClass.getConstructor().newInstance(), tb.select(null), tb, connection);
        } catch (Exception e) {
            throw new TORMException(e);
        }

    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        if(columnName==null||value==null) return new ArrayList<>();
        try {
            return (List<T>) TOUtils.buildItems(tableClass, tableClass.getConstructor().newInstance(), tb.select(TuxJSQL.getBuilder().createWhere().start(columnName, TOUtils.cleanObject(value))), tb, connection);
        } catch (Exception e) {
            throw new TORMException(e);
        }
    }

    @Override
    public void delete(T t) {
        deleteById((ID) TOUtils.getPrimaryKeyValue(tb, t));


    }

    @Override
    public void deleteById(ID t) {
        tb.delete(t);
        for (Field field : t.getClass().getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            TableColumn tc = field.getAnnotation(TableColumn.class);
            String columnName = tc.name().isEmpty() ? field.getName().toLowerCase() : tc.name();
            field.setAccessible(true);
            if (field.getType().isAssignableFrom(List.class)) {
                Table listTable = connection.getListTable(field);
                listTable.delete(sqlBuilder.createWhere().start("parent", t));
            }
        }
    }

    @Override
    public void updateOrCreate(T t) {
        T item = findByID((ID) TOUtils.getPrimaryKeyValue(tb, t));
        if (item == null) {
            create(t);
        } else {
            update(t);
        }
    }

    @Override
    public String getTableName() {
        return tb.getName();
    }


    public ORMConnection getConnection() {
        return connection;
    }

    public Table getTb() {
        return tb;
    }


}

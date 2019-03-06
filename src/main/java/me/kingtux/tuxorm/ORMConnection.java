package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ORMConnection {
    private List<Table> tables = new ArrayList<>();

    private ORMConnection() {
    }

    public static ORMConnection build(Properties settings) {
        TuxJSQL.Type type = TuxJSQL.Type.valueOf(settings.getProperty("type"));
        TuxJSQL.setBuilder(type);
        TuxJSQL.setConnection(settings);
        return new ORMConnection();
    }

    public static ORMConnection build(TuxJSQL.Type type, Connection connection) {
        TuxJSQL.setBuilder(type);
        TuxJSQL.setConnection(connection);
        return new ORMConnection();
    }

    public static ORMConnection build(TuxJSQL.Type type, Properties connection) {
        TuxJSQL.setBuilder(type);
        TuxJSQL.setConnection(connection);
        return new ORMConnection();
    }

    private Table getTableByName(String s) {
        for (Table table : tables) {
            if (table.getName().equalsIgnoreCase(s)) {
                return table;
            }
        }
        return null;
    }

    public <T, ID> Dao<T, ID> createDAO(T t) {
        if (t instanceof Class) {
            registerTable((Class<?>) t);
            return new DaoImpl<>(this, getTableByClass((Class<?>) t), ((Class<?>) t));
        } else {
            registerTable(t.getClass());
            return new DaoImpl<>(this, getTableByClass(t.getClass()), t.getClass());
        }
    }

    public Table getListTable(Field f) {
        TableColumn column = f.getAnnotation(TableColumn.class);
        return getTableByName(column.name().isEmpty() ? f.getName().toLowerCase() : column.name());
    }

    private Table getTableByClass(Class<?> aClass) {
        return getTableByName(TOUtils.getTableName(aClass));
    }

    public void registerTable(Class<?> table) {
        if (getTableByClass(table) != null) return;
        if (table.getAnnotation(DBTable.class) == null) throw new TORMException("Hey add @DBTable to your class!");
        List<Column> columns = new ArrayList<>();
        for (Field field : table.getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            if (!TOUtils.isCompatible(field))
                throw new TORMException("Hey the following is incompatible! " + field.getName() + " " + field.getType().getSimpleName());
            if (field.getType().isAssignableFrom(List.class)) {
                registerListTable(field);
                continue;
            } else if (!TOUtils.isBasic(field.getType())) {
                if (TOUtils.containsFieldWithType(field.getType(), table)) {
                    throw new TORMException("Keep it simple stupid man!");
                } else
                    registerTable(field.getType());
            }
            columns.add(TOUtils.createColumn(field));
        }

        if (columns.isEmpty()) {
            throw new TORMException("Hey no Columns Found!");
        }
        try {
            tables.add(TuxJSQL.getBuilder().createTable(TOUtils.getTableName(table), columns).createIfNotExists());
        } catch (Exception e) {
            throw new TORMException(e);
        }
    }

    private void registerListTable(Field field) {
        TableColumn column = field.getAnnotation(TableColumn.class);
        List<Column> columns = new ArrayList<>();
        columns.add(TuxJSQL.getBuilder().createColumn("id", CommonDataTypes.INT, true));
        columns.add(TuxJSQL.getBuilder().createColumn("parent", CommonDataTypes.INT));
        columns.add(TuxJSQL.getBuilder().createColumn("member", TOUtils.typeFor(TOUtils.getFirstTypeParam(field))));
        tables.add(TuxJSQL.getBuilder().createTable(column.name().isEmpty() ? field.getName().toLowerCase() : column.name(), columns).createIfNotExists());
    }


    protected <T> Object getValue(T t, Object primaryKeyValue) {
        Dao<Object, Object> o = createDAO(t);
        return o.findByID(primaryKeyValue);
    }


}

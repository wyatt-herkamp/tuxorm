package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.TuxJSQL;
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
        registerTable(t.getClass());
        return new DaoImpl<>(this, getTableByClass(t.getClass()), t.getClass());
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
        List<Column> columns = new ArrayList<>();
        for (Field field : table.getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
            if (!TOUtils.isCompatible(field))
                throw new IncompatibleTypeException("Hey the following is incompatible! " + field.getName() + " " + field.getType().getSimpleName());
            if (field.getType().isAssignableFrom(List.class)) {
                registerListTable(field);
                continue;
            } else if (!TOUtils.isbasic(field.getType())) {
                registerTable(field.getType());
            }
            columns.add(TOUtils.createColumn(field));
        }
        //columns.add(TuxJSQL.getBuilder().createColumn("txorm_identifier", CommonDataTypes.INT, false, false, false, true));
        tables.add(TuxJSQL.getBuilder().createTable(TOUtils.getTableName(table), columns).createIfNotExists());
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

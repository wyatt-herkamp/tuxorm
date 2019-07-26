package me.kingtux.tuxorm.tests;

import dev.tuxjsql.basic.sql.BasicDataTypes;
import dev.tuxjsql.core.builders.ColumnBuilder;
import dev.tuxjsql.core.builders.TableBuilder;
import dev.tuxjsql.core.response.DBRow;
import dev.tuxjsql.core.response.DBSelect;
import dev.tuxjsql.core.sql.InsertStatement;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.SQLTable;

import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.tests.objects.Item;

import java.lang.reflect.Field;
import java.util.*;

public class TestSubMMS implements MultiSecondarySerializer<Item> {
    private TOConnection connection;

    public TestSubMMS(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Item item, SQLTable table, Object parentID, Field field) {
        Map<SQLColumn, Object> o = getValues(item, table);
        o.put(table.getColumn(TOUtils.PARENT_ID_NAME), TOUtils.simplifyObject(parentID));
        InsertStatement insertStatement = table.insert();
        o.forEach((sqlColumn, o1) -> {
            insertStatement.value(sqlColumn.getName(), o1);
        });
        insertStatement.execute().queue();
    }

    @Override
    public Item build(DBSelect dbResult, Field field) {
        return minorBuild(dbResult.get(0));
    }

    @Override
    public SQLTable createTable(String name, Field field, SQLDataType parentDataType) {
        List<ColumnBuilder> baseColumns = new ArrayList<>(getColumns());
        baseColumns.add(connection.getBuilder().createColumn().name(TOUtils.PARENT_ID_NAME).setDataType(parentDataType));
        baseColumns.add(connection.getBuilder().createColumn().setDataType(BasicDataTypes.INTEGER).name("id").autoIncrement().primaryKey());
        TableBuilder builder = connection.getBuilder().createTable();
        builder.setName(name);
        baseColumns.forEach(builder::addColumn);

        return builder.createTable();

    }

    @Override
    public List<ColumnBuilder> getColumns(String s) {
        return Arrays.asList(connection.getBuilder().createColumn().name("item" + s).setDataType(BasicDataTypes.INTEGER), connection.getBuilder().createColumn().name("hey" + s).setDataType(BasicDataTypes.TEXT));
    }

    @Override
    public Map<SQLColumn, Object> getValues(Item item, SQLTable table, String s) {
        Map<SQLColumn, Object> objectMap = new HashMap<>();
        objectMap.put(table.getColumn("item" + s), item.getI());
        objectMap.put(table.getColumn("hey" + s), item.getS());
        return objectMap;
    }

    @Override
    public Item minorBuild(DBRow dbRows, String s) {
        return new Item(dbRows.getColumn("hey" + s).get().getAsString(), dbRows.getColumn("item" + s).get().getAsInt());
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }
}

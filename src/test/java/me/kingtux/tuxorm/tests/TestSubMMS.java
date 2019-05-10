package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
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
    public void insert(Item item, Table table, Object parentID, Field field) {
        Map<Column, Object> o = getValues(item, table);
        o.put(table.getColumnByName(TOUtils.PARENT_ID_NAME), TOUtils.simplifyObject(parentID));
        table.insert(o);
    }

    @Override
    public Item build(DBResult dbResult, Field field) {
        return minorBuild(dbResult.get(0));
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        List<Column> baseColumns = new ArrayList<>(getColumns());
        baseColumns.add(connection.getBuilder().createColumn(TOUtils.PARENT_ID_NAME, parentDataType));
        baseColumns.add(connection.getBuilder().createColumn().type(CommonDataTypes.BIGINT).name("id").autoIncrement(true).primary(true).build());
        return connection.getBuilder().createTable(name, baseColumns);
    }

    @Override
    public List<Column> getColumns(String s) {
        return Arrays.asList(connection.getBuilder()
                .createColumn("item"+s, CommonDataTypes.BIGINT), connection.getBuilder().createColumn("hey"+s, CommonDataTypes.TEXT));
    }

    @Override
    public Map<Column, Object> getValues(Item item, Table table, String s) {
        Map<Column, Object> objectMap = new HashMap<>();
        objectMap.put(table.getColumnByName("item"+s), item.getI());
        objectMap.put(table.getColumnByName("hey"+s), item.getS());
        return objectMap;
    }

    @Override
    public Item minorBuild(DBRow dbRows, String s) {
        return new Item(dbRows.getRowItem("hey"+s).getAsString(), dbRows.getRowItem("item"+s).getAsInt());
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }
}

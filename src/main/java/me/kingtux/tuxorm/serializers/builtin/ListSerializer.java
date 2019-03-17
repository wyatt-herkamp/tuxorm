package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.*;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static me.kingtux.tuxorm.TOUtils.*;

public class ListSerializer implements MultiSecondarySerializer<List<?>> {
    private TOConnection connection;

    public ListSerializer(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(List<?> objects, Table table, Object parentID, Field field) {
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        if (isBasic(firstType) || isSemiBasic(firstType)) {
            for (Object object : objects) {
                table.insertAll(parentID, TOUtils.simplifyObject(object));
            }
        } else {
            for (Object object : objects) {
                table.insertAll(parentID, TOUtils.simplifyObject(connection.getPrimaryValue(object)));
            }
        }
    }

    @Override
    public List<?> fetch(Table table, Object parentID, Field field) {
        List<Object> value = new ArrayList<>();
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        ResultSet set = table.select(TuxJSQL.getBuilder().createWhere().start("parent", parentID));
        try {
            if (isBasic(firstType) || isSemiBasic(firstType)) {
                while (set.next()) {
                    value.add(TOUtils.rebuildObject(TOUtils.getFirstTypeParam(field), set.getObject("child")));
                }
            } else {
                while (set.next()) {
                    value.add(connection.quickGet(field.getType(), set.getObject("child")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        Builder builder = TuxJSQL.getBuilder();
        TableBuilder tableBuilder = builder.createTable().name(name);
        tableBuilder.addColumn(builder.createColumn().name("id").primary(true).autoIncrement(true).type(CommonDataTypes.INT).build());
        tableBuilder.addColumn(builder.createColumn().name(PARENT_ID_NAME).type(parentDataType).build());
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        if (isBasic(firstType) || isSemiBasic(firstType)) {
            tableBuilder.addColumn(builder.createColumn().name("child").type(TOUtils.getColumnType(firstType)).build());
        } else {
            tableBuilder.addColumn(builder.createColumn().name("child").type(TOUtils.getColumnType(connection.getPrimaryType(firstType))).build());
        }
        return tableBuilder.build();
    }
}

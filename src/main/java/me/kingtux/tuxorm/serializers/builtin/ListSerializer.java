package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.*;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;

import java.lang.reflect.Field;
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
    public List<?> build(DBResult set, Field field) {
        List<Object> value = new ArrayList<>();
        Class<?> firstType = TOUtils.getFirstTypeParam(field);

        if (isBasic(firstType) || isSemiBasic(firstType)) {
            for (DBRow row : set) {
                value.add(TOUtils.rebuildObject(TOUtils.getFirstTypeParam(field), row.getRowItem("child").getAsObject()));
                }
            } else {
            for (DBRow row : set) {
                value.add(connection.quickGet(field.getType(), row.getRowItem("child").getAsObject()));
                }
            }

        return value;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        SQLBuilder builder = TuxJSQL.getSQLBuilder();
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

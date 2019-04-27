package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

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
                SecondarySerializer ss = connection.getSecondarySerializer(object.getClass());
                if (ss == null) {
                    table.insertAll(parentID, connection.getPrimaryValue(object));
                } else {
                    if (ss instanceof SingleSecondarySerializer) {
                        table.insertAll(parentID, ((SingleSecondarySerializer) ss).getSimplifiedValue(object));
                    }
                }
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
                value.add(TOUtils.quickGet(field.getType(), row.getRowItem("child").getAsObject(), connection));
                }
            }

        return value;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = builder.createTable().name(name);
        tableBuilder.addColumn(builder.createColumn().name("id").primary(true).autoIncrement(true).type(CommonDataTypes.INT).build());
        tableBuilder.addColumn(builder.createColumn().name(PARENT_ID_NAME).type(parentDataType).build());
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        SecondarySerializer ss = connection.getSecondarySerializer(firstType);

        if (isBasic(firstType) || isSemiBasic(firstType)) {
            tableBuilder.addColumn(builder.createColumn().name("child").type(TOUtils.getColumnType(firstType)).build());
        } else if (ss instanceof SingleSecondarySerializer) {
            tableBuilder.addColumn(((SingleSecondarySerializer) ss).createColumn("child"));
        } else if (ss instanceof MultiSecondarySerializer) {
            throw  new IllegalArgumentException("At the moment TuxORM doesnt support MM inside a MM");
        } else {
            tableBuilder.addColumn(builder.createColumn().name("child").type(TOUtils.getColumnType(connection.getPrimaryType(firstType))).build());
        }
        return tableBuilder.build();
    }

    @Override
    public TOConnection getConnection() {
        return connection;
    }
}

package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
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
import java.util.HashMap;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.PARENT_ID_NAME;
import static me.kingtux.tuxorm.TOUtils.isAnyTypeBasic;

public class MapSerializer implements MultiSecondarySerializer<Map<?, ?>> {
    private TOConnection connection;

    public MapSerializer(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Map<?, ?> objects, Table table, Object parentID, Field field) {
        Map<Object, Object> inserts = new HashMap<>();
        for (Map.Entry<?, ?> entry : objects.entrySet()) {
            Object o = getValue(entry.getKey(), TOUtils.getFirstTypeParam(field));
            Object o1 = getValue(entry.getValue(), TOUtils.getTypeParamAt(field, 1));
            inserts.put(o, o1);
        }

        inserts.forEach((o, o2) -> table.insertAll(parentID, o, o2));

    }

    private Object getValue(Object o, Class<?> type) {
        SecondarySerializer ss = connection.getSecondarySerializer(type);
        if (ss == null) {
            if (isAnyTypeBasic(o.getClass())) {
                return TOUtils.simplifyObject(o);
            } else {
                return connection.getPrimaryValue(o);
            }
        } else {
            if (ss instanceof SingleSecondarySerializer) {
                return ((SingleSecondarySerializer) ss).getSimplifiedValue(o);
            }
        }
        return null;
    }

    @Override
    public Map<?, ?> build(DBResult set, Field field) {
        Map<Object, Object> value = new HashMap<>();
        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        Map<Object, Object> values = new HashMap<>();
        for (DBRow row : set) {
            values.put(getBuild(row.getRowItem("key").getAsObject(), type1), getBuild(row.getRowItem("value").getAsObject(), type2));
        }
        return values;
    }

    private Object getBuild(Object o, Class<?> type) {
        SecondarySerializer ss = connection.getSecondarySerializer(type);
        if (ss == null) {
            if (isAnyTypeBasic(o.getClass())) {
                return TOUtils.rebuildObject(type, o);
            } else {
                return TOUtils.quickGet(type, o, connection);
            }
        } else {
            if (ss instanceof SingleSecondarySerializer) {
                return ((SingleSecondarySerializer) ss).buildFromSimplifiedValue(o);
            }
        }
        return null;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = builder.createTable().name(name);
        tableBuilder.addColumn(builder.createColumn().name("id").primary(true).autoIncrement(true).type(CommonDataTypes.INT).build());
        tableBuilder.addColumn(builder.createColumn().name(PARENT_ID_NAME).type(parentDataType).build());
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        SecondarySerializer ss = connection.getSecondarySerializer(firstType);

        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        tableBuilder.addColumn(getColumn("key", type1));
        tableBuilder.addColumn(getColumn("value", type2));

        return tableBuilder.build();
    }

    Column getColumn(String value, Class<?> type) {
        if (connection.getSecondarySerializer(type) == null) {
            if (isAnyTypeBasic(type)) {
                return connection.getBuilder().createColumn().type(TOUtils.getColumnType(type)).name(value).build();

            } else {
                return connection.getBuilder().createColumn().type(TOUtils.getColumnType(connection.getPrimaryType(type))).name(value).build();

            }
        } else {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(type);
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                return ((SingleSecondarySerializer) secondarySerializer).createColumn(value);
            }
        }
        return null;
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }
}

package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.result.ColumnItem;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;
import me.kingtux.tuxorm.serializers.SubMSSCompatible;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.*;

public class MapSerializer implements MultiSecondarySerializer<Map<?, ?>> {
    private TOConnection connection;
    private static final String VALUE = "value";
    private static final String KEY = "key";

    public MapSerializer(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Map<?, ?> objects, Table table, Object parentID, Field field) {
        for (Map.Entry<?, ?> entry : objects.entrySet()) {
            Map<Column, Object> inserts = new HashMap<>();
            inserts.put(table.getColumnByName(PARENT_ID_NAME), parentID);
            getValue(entry.getKey(), TOUtils.getTypeParamAt(field, 0), table, KEY).forEach(inserts::put);
            getValue(entry.getValue(), TOUtils.getTypeParamAt(field, 1), table, VALUE).forEach(inserts::put);
            table.insert(inserts);
        }
    }

    private Map<Column, Object> getValue(Object o, Class<?> type, Table table, String key) {
        SecondarySerializer ss = connection.getSecondarySerializer(type);
        if (ss == null) {
            if (isAnyTypeBasic(o.getClass())) {
                Map<Column, Object> map = new HashMap();
                map.put(table.getColumnByName(key), simplifyObject(o));
                return map;
            } else {
                Map<Column, Object> map = new HashMap();
                map.put(table.getColumnByName(key), connection.getPrimaryValue(o));
                return map;
            }
        } else {
            if (ss instanceof SingleSecondarySerializer) {
                Map<Column, Object> map = new HashMap();
                map.put(table.getColumnByName(key), ((SingleSecondarySerializer) ss).getSimplifiedValue(o));
                return map;
            } else if (ss instanceof SubMSSCompatible) {
                return ((SubMSSCompatible) ss).getValues(o, table,key);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<?, ?> build(DBResult set, Field field) {
        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        Map<Object, Object> values = new HashMap<>();
        for (DBRow row : set) {
            Object o =getBuild(row.getRowItem(KEY), type1, row, KEY);
            Object o2 =getBuild(row.getRowItem(VALUE), type2, row, VALUE);
            values.put(o, o2);
        }
        return values;
    }

    private Object getBuild(ColumnItem o, Class<?> type, DBRow dbRow, String value) {
        SecondarySerializer ss = connection.getSecondarySerializer(type);
        if (ss == null) {
            if (isAnyTypeBasic(type)) {
                return TOUtils.rebuildObject(type, o.getAsObject());
            } else {
                return TOUtils.quickGet(type, o.getAsObject(), connection);
            }
        } else {
            if (ss instanceof SingleSecondarySerializer) {
                return ((SingleSecondarySerializer) ss).buildFromSimplifiedValue(o.getAsObject());
            } else if (ss instanceof SubMSSCompatible) {
                return ((SubMSSCompatible) ss).minorBuild(dbRow, value);
            }
        }
        return null;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = TOUtils.basicTable(builder, name, parentDataType);

        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        getColumn(KEY, type1).forEach(tableBuilder::addColumn);
        getColumn(VALUE, type2).forEach(tableBuilder::addColumn);


        return tableBuilder.build();
    }


    List<Column> getColumn(String value, Class<?> type) {
        if (connection.getSecondarySerializer(type) == null) {
            if (isAnyTypeBasic(type)) {
                return Collections.singletonList(connection.getBuilder().createColumn().type(getColumnType(type)).name(value).build());

            } else {
                return Collections.singletonList(connection.getBuilder().createColumn().type(getColumnType(connection.getPrimaryType(type))).name(value).build());

            }
        } else {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(type);
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                return Collections.singletonList(((SingleSecondarySerializer) secondarySerializer).createColumn(value));
            } else if (secondarySerializer instanceof SubMSSCompatible) {
                return ((SubMSSCompatible) secondarySerializer).getColumns(value);
            }
        }
        return Collections.emptyList();
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }
}

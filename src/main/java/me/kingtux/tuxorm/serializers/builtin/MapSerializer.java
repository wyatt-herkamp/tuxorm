package me.kingtux.tuxorm.serializers.builtin;

import dev.tuxjsql.core.builders.ColumnBuilder;
import dev.tuxjsql.core.builders.SQLBuilder;
import dev.tuxjsql.core.builders.TableBuilder;
import dev.tuxjsql.core.response.DBColumnItem;
import dev.tuxjsql.core.response.DBRow;
import dev.tuxjsql.core.response.DBSelect;
import dev.tuxjsql.core.sql.InsertStatement;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.annotations.DataType;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.MultipleValueSerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.*;

public class MapSerializer implements MultipleValueSerializer<Map<?, ?>> {
    private TOConnection connection;
    private static final String VALUE = "value";
    private static final String KEY = "key";

    public MapSerializer(TOConnection connection) {
        this.connection = connection;
    }

    @Override
    public void insert(Map<?, ?> objects, SQLTable table, Object parentID, Field field) {
        for (Map.Entry<?, ?> entry : objects.entrySet()) {
            Map<SQLColumn, Object> inserts = new HashMap<>();
            inserts.put(table.getColumn(PARENT_ID_NAME), parentID);
            getValue(entry.getKey(), TOUtils.getTypeParamAt(field, 0), table, KEY).forEach(inserts::put);
            getValue(entry.getValue(), TOUtils.getTypeParamAt(field, 1), table, VALUE).forEach(inserts::put);
            InsertStatement insertStatement = table.insert();
            inserts.forEach((sqlColumn, o1) -> {
                insertStatement.value(sqlColumn.getName(), o1);
            });
            insertStatement.execute().queue();        }
    }

    private Map<SQLColumn, Object> getValue(Object o, Class<?> type, SQLTable table, String key) {
        SecondarySerializer ss = connection.getSecondarySerializer(type);
        if (ss == null) {
            if (isAnyTypeBasic(o.getClass())) {
                Map<SQLColumn, Object> map = new HashMap();
                map.put(table.getColumn(key), simplifyObject(o));
                return map;
            } else {
                Map<SQLColumn, Object> map = new HashMap();
                map.put(table.getColumn(key), connection.getPrimaryValue(o));
                return map;
            }
        } else {
            if (ss instanceof SingleSecondarySerializer) {
                Map<SQLColumn, Object> map = new HashMap();
                map.put(table.getColumn(key), ((SingleSecondarySerializer) ss).getSimplifiedValue(o));
                return map;
            } else if (ss instanceof MultiSecondarySerializer && !(ss instanceof MultipleValueSerializer)) {
                return ((MultiSecondarySerializer) ss).getValues(o, table, key);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<?, ?> build(DBSelect set, Field field) {
        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        Map<Object, Object> values = new HashMap<>();
        for (DBRow row : set) {
            Object o =getBuild(row.getRow(KEY), type1, row, KEY);
            Object o2 =getBuild(row.getRow(VALUE), type2, row, VALUE);
            values.put(o, o2);
        }
        return values;
    }

    private Object getBuild(DBColumnItem o, Class<?> type, DBRow dbRow, String value) {
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
            } else if (ss instanceof MultiSecondarySerializer) {
                return ((MultiSecondarySerializer) ss).minorBuild(dbRow, value);
            }
        }
        return null;
    }

    @Override
    public SQLTable createTable(String name, Field field, SQLDataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = TOUtils.basicTable(builder, name, parentDataType);

        Class<?> type1 = TOUtils.getTypeParamAt(field, 0);
        Class<?> type2 = TOUtils.getTypeParamAt(field, 1);
        getColumn(KEY, type1).forEach(tableBuilder::addColumn);
        getColumn(VALUE, type2).forEach(tableBuilder::addColumn);


        return tableBuilder.createTable();
    }


    List<ColumnBuilder> getColumn(String value, Class<?> type) {
        if (connection.getSecondarySerializer(type) == null) {
            if (isAnyTypeBasic(type)) {
                return Collections.singletonList(connection.getBuilder().createColumn().setDataType(getColumnType(type)).name(value));

            } else {
                return Collections.singletonList(connection.getBuilder().createColumn().setDataType(getColumnType(connection.getPrimaryType(type))).name(value));

            }
        } else {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(type);
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                return Collections.singletonList(((SingleSecondarySerializer) secondarySerializer).createColumn(value));
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                return ((MultiSecondarySerializer) secondarySerializer).getColumns(value);
            }
        }
        return Collections.emptyList();
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public List<Object> contains(Object o, SQLTable table) {
        return TOUtils.contains(o, table, connection, KEY);
    }
}

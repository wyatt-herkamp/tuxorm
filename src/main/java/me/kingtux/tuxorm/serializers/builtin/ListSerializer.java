package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOException;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.MultipleValueSerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.*;

public class ListSerializer implements MultipleValueSerializer<List<?>> {
    private TOConnection connection;
    private static final String CHILD = "child";
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
                    } else if (ss instanceof MultiSecondarySerializer) {
                        Map<Column, Object> o = ((MultiSecondarySerializer) ss).getValues(object, table);
                        o.put(table.getColumnByName(PARENT_ID_NAME), parentID);
                        table.insert(o);
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
                value.add(TOUtils.rebuildObject(TOUtils.getFirstTypeParam(field), row.getRowItem(CHILD).getAsObject()));
                }
        } else if (connection.getSecondarySerializer(firstType) != null) {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(firstType);
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                for (DBRow row : set) {
                    value.add(((SingleSecondarySerializer) secondarySerializer).buildFromSimplifiedValue(row.getRowItem(CHILD).getAsObject()));
                }
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                MultiSecondarySerializer mssCompatible = (MultiSecondarySerializer) secondarySerializer;
                for (DBRow row : set) {
                    value.add(mssCompatible.minorBuild(row));
                }
            }
        } else {
            for (DBRow row : set) {
                value.add(TOUtils.quickGet(field.getType(), row.getRowItem(CHILD).getAsObject(), connection));
            }
        }
        return value;
    }

    @Override
    public Table createTable(String name, Field field, DataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = TOUtils.basicTable(builder, name, parentDataType);

        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        SecondarySerializer ss = connection.getSecondarySerializer(firstType);

        if (isBasic(firstType) || isSemiBasic(firstType)) {
            tableBuilder.addColumn(builder.createColumn().name(CHILD).type(TOUtils.getColumnType(firstType)).build());
        } else if (ss instanceof SingleSecondarySerializer) {
            tableBuilder.addColumn(((SingleSecondarySerializer) ss).createColumn(CHILD));
        } else if (ss instanceof MultiSecondarySerializer) {
            if (ss instanceof MultipleValueSerializer) {
                throw new TOException("Cant have a MultipleValue in a MultipleValue");
            }
            if (ss instanceof MultiSecondarySerializer) {
                MultiSecondarySerializer smss = ((MultiSecondarySerializer) ss);
                for (Object c : smss.getColumns()) {
                    tableBuilder.addColumn((Column) c);

                }
            } else {
                throw new IllegalArgumentException("This MSS is incompatible with SubMSS");
            }
        } else {
            tableBuilder.addColumn(builder.createColumn().name(CHILD).type(TOUtils.getColumnType(connection.getPrimaryType(firstType))).build());
        }
        return tableBuilder.build();
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public List<Object> contains(Object o, Table table) {
        return TOUtils.contains(o, table, connection, CHILD);
    }
}

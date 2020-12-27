package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.response.DBRow;
import me.kingtux.tuxjsql.core.response.DBSelect;
import me.kingtux.tuxjsql.core.sql.InsertStatement;
import me.kingtux.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxjsql.core.sql.SQLDataType;
import me.kingtux.tuxjsql.core.sql.SQLTable;

import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOException;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.annotations.DataType;
import me.kingtux.tuxorm.exceptions.MissingValueException;
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
    public void insert(List<?> objects, SQLTable table, Object parentID, Field field) {
        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        if (isBasic(firstType) || isSemiBasic(firstType)) {
            for (Object object : objects) {
                table.insert().value(PARENT_ID_NAME, parentID).value(CHILD, TOUtils.simplifyObject(object)).execute().queue();
            }
        } else {
            for (Object object : objects) {
                SecondarySerializer ss = connection.getSecondarySerializer(object.getClass());
                if (ss == null) {
                    table.insert().value(PARENT_ID_NAME, parentID).value(CHILD, connection.getPrimaryValue(object)).execute().queue();

                } else {
                    if (ss instanceof SingleSecondarySerializer) {
                        table.insert().value(PARENT_ID_NAME, parentID).value(CHILD, ((SingleSecondarySerializer) ss).getSimplifiedValue(object)).execute().queue();
                    } else if (ss instanceof MultiSecondarySerializer) {
                        Map<SQLColumn, Object> o = ((MultiSecondarySerializer) ss).getValues(object, table);
                        o.put(table.getColumn(PARENT_ID_NAME), parentID);
                        InsertStatement insertStatement = table.insert();
                        o.forEach((sqlColumn, o1) -> insertStatement.value(sqlColumn.getName(), o1));
                        insertStatement.execute().queue();
                    }
                }
            }
        }
    }

    @Override
    public List<?> build(DBSelect set, Field field) {
        List<Object> value = new ArrayList<>();
        Class<?> firstType = TOUtils.getFirstTypeParam(field);

        if (isBasic(firstType) || isSemiBasic(firstType)) {
            for (DBRow row : set) {
                value.add(TOUtils.rebuildObject(TOUtils.getFirstTypeParam(field), row.getColumn(CHILD).orElseThrow(()-> new MissingValueException(CHILD+ " is misisng from your system")).getAsObject()));
            }
        } else if (connection.getSecondarySerializer(firstType) != null) {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(firstType);
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                for (DBRow row : set) {
                    value.add(((SingleSecondarySerializer) secondarySerializer).buildFromSimplifiedValue(row.getColumn(CHILD).get().getAsObject()));
                }
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                MultiSecondarySerializer mssCompatible = (MultiSecondarySerializer) secondarySerializer;
                for (DBRow row : set) {
                    value.add(mssCompatible.minorBuild(row));
                }
            }
        } else {
            for (DBRow row : set) {
                value.add(TOUtils.quickGet(field.getType(), row.getColumn(CHILD).orElseThrow(()-> new MissingValueException(CHILD+ " is misisng from your system")).getAsObject(), connection));
            }
        }
        return value;
    }

    @Override
    public SQLTable createTable(String name, Field field, SQLDataType parentDataType) {
        SQLBuilder builder = connection.getBuilder();
        TableBuilder tableBuilder = TOUtils.basicTable(builder, name, parentDataType);

        Class<?> firstType = TOUtils.getFirstTypeParam(field);
        SecondarySerializer ss = connection.getSecondarySerializer(firstType);

        if (isBasic(firstType) || isSemiBasic(firstType)) {
            tableBuilder.addColumn(builder.createColumn().name(CHILD).setDataType(TOUtils.getColumnType(firstType)));
        } else if (ss instanceof SingleSecondarySerializer) {
            tableBuilder.addColumn(((SingleSecondarySerializer) ss).createColumn(CHILD));
        } else if (ss instanceof MultiSecondarySerializer) {
            if (ss instanceof MultipleValueSerializer) {
                throw new TOException("Cant have a MultipleValue in a MultipleValue");
            }
            if (ss instanceof MultiSecondarySerializer) {
                MultiSecondarySerializer smss = ((MultiSecondarySerializer) ss);
                for (Object c : smss.getColumns()) {
                    tableBuilder.addColumn((ColumnBuilder) c);

                }
            } else {
                throw new IllegalArgumentException("This MSS is incompatible with SubMSS");
            }
        } else {
            tableBuilder.addColumn(builder.createColumn().name(CHILD).setDataType(TOUtils.getColumnType(connection.getPrimaryType(firstType))));
        }
        return tableBuilder.createTable();
    }


    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public List<Object> contains(Object o, SQLTable table) {
        return TOUtils.contains(o, table, connection, CHILD);
    }
}

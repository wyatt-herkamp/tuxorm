package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.DataType;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxjsql.core.builders.TableBuilder;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOUtils;
import me.kingtux.tuxorm.serializers.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.*;

public class ListSerializer implements MultiSecondarySerializer<List<?>>, MultipleValueSerializer {
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
                    } else if (ss instanceof SubMSSCompatible) {
                        Map<Column, Object> o = ((SubMSSCompatible) ss).getValues(object, table);
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
            } else if (secondarySerializer instanceof SubMSSCompatible) {
                SubMSSCompatible mssCompatible = (SubMSSCompatible) secondarySerializer;
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
            if (ss instanceof SubMSSCompatible) {
                SubMSSCompatible smss = ((SubMSSCompatible) ss);
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
        List<Object> objects = new ArrayList<>();
        DBResult result = null;
        if (isAnyTypeBasic(o.getClass())) {
            result = table.select(connection.getBuilder().createWhere().start(CHILD, o));

        } else {
            SecondarySerializer ss = connection.getSecondarySerializer(o.getClass());
            if (ss == null) {
                result = table.select(connection.getBuilder().createWhere().start(CHILD, connection.getPrimaryValue(o)));
            } else {
                if (ss instanceof SingleSecondarySerializer) {
                    result = table.select(connection.getBuilder().createWhere().start(CHILD, ((SingleSecondarySerializer) ss).getSimplifiedValue(o)));

                } else if (ss instanceof SubMSSCompatible) {
                    SubMSSCompatible mssCompatible = (SubMSSCompatible) ss;
                    WhereStatement where = connection.getBuilder().createWhere();
                    Map<Column, Object> map = mssCompatible.getValues(o, table);
                    int i = 0;
                    for (Map.Entry<Column, Object> value : map.entrySet()) {
                        if (i == 0) {
                            where.start(value.getKey().getName(), value.getValue());
                        } else {
                            where.AND(value.getKey().getName(), value.getValue());
                        }
                        i++;
                    }
                    result = table.select(where);
                }
            }
        }
        if (result == null) return Collections.emptyList();
        for (DBRow row : result) {
            objects.add(rebuildObject(o.getClass(), row.getRowItem(PARENT_ID_NAME).getAsObject()));
        }
        return objects;
    }
}

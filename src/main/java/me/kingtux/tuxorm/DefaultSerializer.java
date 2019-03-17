package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.*;
import me.kingtux.tuxorm.annotations.TableColumn;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.isAnyTypeBasic;
import static me.kingtux.tuxorm.TOUtils.simpleClass;

public final class DefaultSerializer {

    private TOConnection toConnection;

    public DefaultSerializer(TOConnection toConnection) {
        this.toConnection = toConnection;
    }

    public Class<?> getPrimaryKeyType(Class<?> firstType) {
        for (Field field : firstType.getDeclaredFields()) {
            TableColumn tc = field.getAnnotation(TableColumn.class);
            if (tc == null) continue;
            if (tc.primary()) return field.getType();
        }
        return null;
    }

    public Object getPrimaryKey(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            TableColumn tc = field.getAnnotation(TableColumn.class);
            if (tc == null) continue;
            if (tc.primary()) return field.getType();
            field.setAccessible(true);
            try {
                field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public TOObject createTable(Class<?> tableClass) {
        List<Column> columns = new ArrayList<>();
        Map<Field, Table> extraTables = new HashMap<>();
        Builder builder = TuxJSQL.getBuilder();
        String tName = TOUtils.getClassName(tableClass);
        for (Field field : tableClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (isAnyTypeBasic(field.getType())) {
                columns.add(createColumn(field));
            } else {
                SecondarySerializer ss = toConnection.getSecondarySerializer(field.getType());
                if (ss != null) {
                    if (ss instanceof SingleSecondarySerializer) {
                        columns.add(((SingleSecondarySerializer) ss).createColumn(TOUtils.getFieldName(field)));
                    } else if (ss instanceof MultiSecondarySerializer) {
                        extraTables.put(field,
                                ((MultiSecondarySerializer) ss).createTable(TOUtils.getFieldName(field) + "_" + tName,
                                        field,
                                        TOUtils.getColumnType(TOUtils.simpleClass(getPrimaryKeyType(tableClass)))));
                    } else {
                        throw new RuntimeException("Something broke(I DONT CARE)(WILL HANDLE BETTER)");
                    }
                } else {
                    ColumnBuilder cb = builder.createColumn();
                    cb.name(TOUtils.getFieldName(field)).type(TOUtils.getColumnType(TOUtils.simpleClass(getPrimaryKeyType(tableClass))));
                    columns.add(cb.build());
                }
            }
        }
        return new TOObject(tableClass, builder.createTable(tName, columns), extraTables);
    }

    public Column createColumn(Field field) {
        Builder builder = TuxJSQL.getBuilder();
        return builder.createColumn().name(TOUtils.getFieldName(field)).type(TOUtils.getColumnType(simpleClass(field.getType()))).build();
    }
}

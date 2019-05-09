package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.kingtux.tuxorm.TOUtils.*;

@SuppressWarnings("All")
public final class DefaultSerializer {

    private TOConnection toConnection;
    private Map<Class<?>, TOObject> objects = new HashMap<>();

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

    public void update(Object value, TOObject toObject) {
        Object primaryKeyValue = getPrimaryKey(value);
        if(primaryKeyValue==null){
            throw new TOException("Hey unable to locate a primarykey for " + value.getClass().getSimpleName());
        }

        try {
            for (Map.Entry<Field, Table> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.delete(primaryKeyValue, extraTables.getKey(), extraTables.getValue());
            }
            Map<Column, Object> update = new HashMap<>();
            for (Field field : value.getClass().getDeclaredFields()) {
                TableColumn tc = field.getAnnotation(TableColumn.class);
                if (tc == null) continue;
                field.setAccessible(true);
                if (isAnyTypeBasic(field.getType())) {
                    update.put(toObject.getColumnForField(field), TOUtils.simplifyObject(field.get(value)));
                } else if (toConnection.getSecondarySerializer(field.getType()) != null) {
                    SecondarySerializer serializer = toConnection.getSecondarySerializer(field.getType());
                    if (serializer instanceof SingleSecondarySerializer) {
                        update.put(toObject.getColumnForField(field), ((SingleSecondarySerializer) serializer).getSimplifiedValue(field.get(value)));
                    }
                } else {
                    Object pkey = null;
                    if (field.get(value) != null) {
                        pkey = toConnection.getPrimaryValue(field.get(value));
                        if (pkey == null || TOUtils.isPrimitveNull(pkey)) {
                            pkey = toConnection.getPrimaryValue(TOUtils.quickInsert(field.get(value), toConnection));
                        }
                    }
                    update.put(toObject.getColumnForField(field), pkey);
                }
            }
            toObject.getTable().update(primaryKeyValue, update);
            for (Map.Entry<Field, Table> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.insert(extraTables.getKey().get(value), extraTables.getValue(), primaryKeyValue, extraTables.getKey());
            }
        } catch (IllegalAccessException e) {
            TOConnection.logger.error("Unable to access field", e);
        }
    }

    public Object create(Object value, TOObject toObject) {
        Object primaryKeyValue = null;
        String primaryKeyName = "";

        try {
            Map<Column, Object> insert = new HashMap<>();
            for (Field field : value.getClass().getDeclaredFields()) {
                TableColumn tc = field.getAnnotation(TableColumn.class);
                if (tc == null) continue;
                field.setAccessible(true);
                if (tc.primary()) {
                    if (!tc.autoIncrement()) {
                        primaryKeyValue = field.get(value);
                    } else {
                        primaryKeyName = TOUtils.getColumnNameByField(field);
                    }
                }
                if (isAnyTypeBasic(field.getType())) {
                    insert.put(toObject.getColumnForField(field), TOUtils.simplifyObject(field.get(value)));
                } else if (toConnection.getSecondarySerializer(field.getType()) != null) {
                    SecondarySerializer serializer = toConnection.getSecondarySerializer(field.getType());
                    if (serializer instanceof SingleSecondarySerializer) {
                        insert.put(toObject.getColumnForField(field), ((SingleSecondarySerializer) serializer).getSimplifiedValue(field.get(value)));
                    }
                } else {
                    Object pkey = null;
                    if (field.get(value) != null) {
                        pkey = toConnection.getPrimaryValue(field.get(value));
                        if (pkey == null || TOUtils.isPrimitveNull(pkey)) {
                            pkey = toConnection.getPrimaryValue(TOUtils.quickInsert(field.get(value), toConnection));
                        }
                    }
                    insert.put(toObject.getColumnForField(field), pkey);
                }
            }
            toObject.getTable().insert(insert);

            if (primaryKeyValue == null) {
                primaryKeyValue = toObject.getTable().max(primaryKeyName);
            }
            for (Map.Entry<Field, Table> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.insert(extraTables.getKey().get(value), extraTables.getValue(), primaryKeyValue, extraTables.getKey());
            }
        } catch (IllegalAccessException e) {
            TOConnection.logger.error("Unable to access variable", e);
        }
        return primaryKeyValue;
    }

    public Object getPrimaryKey(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            TableColumn tc = field.getAnnotation(TableColumn.class);
            if (tc == null || !tc.primary()) continue;
            field.setAccessible(true);
            try {
                return field.get(object);
            } catch (IllegalAccessException e) {
                TOConnection.logger.error("Unable to access variable", e);
            }
        }
        return null;
    }

    public void createTable(Class<?> tableClass) {
        if(tableClass==null){
            throw new IllegalArgumentException("the provided class is null");
        }
        if(tableClass.getAnnotation(DBTable.class)==null){
            throw new IllegalArgumentException(tableClass.getName() + " is missing required @DBTable");
        }
        try {
            tableClass.getConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new TOException("Unable to find public constructor!");
        }
        //Now get to work
        List<Column> columns = new ArrayList<>();
        Map<Field, Table> extraTables = new HashMap<>();
        SQLBuilder builder = toConnection.getBuilder();
        String tName = TOUtils.getClassName(tableClass);
        for (Field field : tableClass.getDeclaredFields()) {
            if(field.getAnnotation(TableColumn.class)==null)  continue;
            field.setAccessible(true);
            if (isAnyTypeBasic(field.getType())) {
                columns.add(createColumn(field));
            } else {
                SecondarySerializer ss = toConnection.getSecondarySerializer(field.getType());
                if (ss != null) {
                    if (ss instanceof SingleSecondarySerializer) {
                        columns.add(((SingleSecondarySerializer) ss).createColumn(TOUtils.getColumnNameByField(field)));
                    } else if (ss instanceof MultiSecondarySerializer) {
                        extraTables.put(field,
                                ((MultiSecondarySerializer) ss).createTable(TOUtils.getColumnNameByField(field) + "_" + tName,
                                        field,
                                        TOUtils.getColumnType(TOUtils.simpleClass(getPrimaryKeyType(tableClass)))));
                    }
                } else {
                    ColumnBuilder cb = builder.createColumn();
                    cb.name(TOUtils.getColumnNameByField(field)).type(TOUtils.getColumnType(TOUtils.simpleClass(getPrimaryKeyType(tableClass))));
                    columns.add(cb.build());
                }
            }
        }
        objects.put(tableClass, new TOObject(tableClass, builder.createTable(tName, columns).createUpdate(), extraTables));
        extraTables.forEach((field, table) -> table.createUpdate());
    }

    public Column createColumn(Field field) {
        TableColumn tableColumn  = field.getAnnotation(TableColumn.class);
        ColumnBuilder builder = toConnection.getBuilder().createColumn().name(TOUtils.getColumnNameByField(field)).
                type(TOUtils.getColumnType(simpleClass(field.getType()))).primary(tableColumn.primary()).autoIncrement(tableColumn.autoIncrement()).notNull(tableColumn.notNull());
        if (tableColumn.useDefault()) {
            Object o = null;
            try {
                Object item = field.getDeclaringClass().getConstructor().newInstance();
                o = field.get(item);
            } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
                TOConnection.logger.error("Unable create instance of class", e);
            }
            if (o != null && isAnyTypeBasic(o.getClass())) {
                    builder.defaultValue(simplifyObject(o));

            }
        }
        return builder.build();
    }

    public <T> T build(Class<?> item, TOResult toResult, TOObject object) {
        try {
            T t = (T) item.getConstructor().newInstance();
            for (Map.Entry<Field, TableResult> entry : toResult.getExtraTables().entrySet()) {
                entry.getKey().setAccessible(true);
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(entry.getKey().getType());
                entry.getKey().set(t, mss.build(entry.getValue().getResult(), entry.getKey()));
            }
            for (Field field : item.getDeclaredFields()) {

                field.setAccessible(true);
                TableColumn tc = field.getAnnotation(TableColumn.class);

                if (toResult.getExtraTables().containsKey(field)
                        || tc == null ||
                        toResult.getPrimaryTable().getRow().getRowItem(object.getColumnForField(field).getName()).getAsObject()==null) continue;

                Object value = toResult.getPrimaryTable().getRow().getRowItem(object.getColumnForField(field).getName()).getAsObject();

                if (isAnyTypeBasic(field.getType())) {
                    field.set(t, rebuildObject(field.getType(), value));
                } else if (toConnection.getSecondarySerializer(field.getType()) != null) {
                    SecondarySerializer serializer = toConnection.getSecondarySerializer(field.getType());
                    if (serializer instanceof SingleSecondarySerializer) {
                        field.set(t, ((SingleSecondarySerializer) serializer).buildFromSimplifiedValue(value));
                    }
                } else {
                    field.set(t, TOUtils.quickGet(field.getType(), value, toConnection));
                }
            }
            return t;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            TOConnection.logger.error("Unable to work", e);
        }
        return null;
    }

    public void delete(Object value, TOObject toObject) {
        Object o = getPrimaryKey(value);
        if(o==null) return;
        toObject.getTable().delete(toConnection.getBuilder().createWhere().start(toObject.getTable().getPrimaryColumn().getName(), o));
        for (Map.Entry<Field, Table> entry : toObject.getOtherObjects().entrySet()) {
            Table table = entry.getValue();
            table.delete(toConnection.getBuilder().createWhere().start(PARENT_ID_NAME, o));
        }
    }

    public  TOObject getToObject(Class<?> type) {
        return objects.get(type);
    }
}

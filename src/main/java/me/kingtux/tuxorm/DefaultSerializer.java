package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.*;
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
                    Object pkey = toConnection.getPrimaryValue(value);
                    if (pkey == null) {
                        pkey = toConnection.quickInsert(value);
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
            e.printStackTrace();
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
                        primaryKeyName = TOUtils.getFieldName(field);
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
                    Object pkey = toConnection.getPrimaryValue(value);
                    if (pkey == null) {
                        pkey = toConnection.quickInsert(value);
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
            e.printStackTrace();
        }
        return primaryKeyValue;
    }

    public Object getPrimaryKey(Object object) {
        for (Field field : object.getClass().getDeclaredFields()) {
            TableColumn tc = field.getAnnotation(TableColumn.class);
            if (tc == null) continue;
            if (!tc.primary()) continue;
            field.setAccessible(true);
            try {
                field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void createTable(Class<?> tableClass) {
        List<Column> columns = new ArrayList<>();
        Map<Field, Table> extraTables = new HashMap<>();
        SQLBuilder builder = TuxJSQL.getSQLBuilder();
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
        objects.put(tableClass, new TOObject(tableClass, builder.createTable(tName, columns).createIfNotExists(), extraTables));
        extraTables.forEach((field, table) -> {
            table.createIfNotExists();
        });
    }

    public Column createColumn(Field field) {
        TableColumn tableColumn  = field.getAnnotation(TableColumn.class);
        return Column.create().name(TOUtils.getFieldName(field)).
                type(TOUtils.getColumnType(simpleClass(field.getType()))).primary(tableColumn.primary()).autoIncrement(tableColumn.autoIncrement()).build();
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
                if (toResult.getExtraTables().containsKey(field)) {
                    continue;
                }
                field.setAccessible(true);
                TableColumn tc = field.getAnnotation(TableColumn.class);
                if (tc == null) continue;
                Object value = toResult.getPrimaryTable().getRow().getRowItem(object.getColumnForField(field).getName()).getAsObject();
                if (isAnyTypeBasic(field.getType())) {
                    field.set(t, rebuildObject(field.getType(), value));
                } else if (toConnection.getSecondarySerializer(field.getType()) != null) {
                    SecondarySerializer serializer = toConnection.getSecondarySerializer(field.getType());
                    if (serializer instanceof SingleSecondarySerializer) {
                        field.set(t, ((SingleSecondarySerializer) serializer).buildFromSimplifiedValue(value));
                    }
                } else {
                    field.set(t, toConnection.quickGet(field.getType(), value));
                }
            }
            return t;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void delete(Object value, TOObject toObject) {
        Object o = getPrimaryKey(value);
        toObject.getTable().delete(o);
        toObject.getOtherObjects().forEach((field, table) -> {
            table.delete(o);
        });
    }

    public  TOObject getToObject(Class<?> type) {
        return objects.get(type);
    }
}

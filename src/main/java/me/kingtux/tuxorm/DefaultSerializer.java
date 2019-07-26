package me.kingtux.tuxorm;


import dev.tuxjsql.core.builders.ColumnBuilder;
import dev.tuxjsql.core.builders.SQLBuilder;
import dev.tuxjsql.core.builders.TableBuilder;
import dev.tuxjsql.core.response.DBInsert;
import dev.tuxjsql.core.sql.InsertStatement;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLTable;
import dev.tuxjsql.core.sql.UpdateStatement;
import me.kingtux.tuxorm.annotations.TableColumn;
import me.kingtux.tuxorm.exceptions.MissingValueException;
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
        Object primaryKeyValue = TOValidator.validateUpdate(value, toObject, this);
        try {
            for (Map.Entry<Field, SQLTable> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.delete(primaryKeyValue, extraTables.getKey(), extraTables.getValue());
            }
            Map<SQLColumn, Object> update = new HashMap<>();
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
            //insert
            UpdateStatement statement = toObject.getTable().update().where().start(toObject.getTable().getPrimaryColumn().getName(), primaryKeyValue).and();
            update.forEach((sqlColumn, o) -> statement.value(sqlColumn.getName(), o));
            statement.execute().complete();

            for (Map.Entry<Field, SQLTable> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.insert(extraTables.getKey().get(value), extraTables.getValue(), primaryKeyValue, extraTables.getKey());
            }
        } catch (IllegalAccessException e) {
            TOConnection.logger.error("Unable to access field", e);
        } catch (InterruptedException e) {
            TOConnection.logger.error("Unable to get value", e);
            Thread.currentThread().interrupt();
        }
    }

    public Object create(Object value, TOObject toObject) {
        TOValidator.validateCreate(value, toObject);
        Object primaryKeyValue = null;
        try {
            Map<SQLColumn, Object> insert = new HashMap<>();
            for (Field field : value.getClass().getDeclaredFields()) {
                TableColumn tc = field.getAnnotation(TableColumn.class);
                if (tc == null) continue;
                field.setAccessible(true);
                if (tc.primary()) {
                    if (!tc.autoIncrement()) {

                        primaryKeyValue = field.get(value);
                    } else {
                        continue;
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

            InsertStatement statement = toObject.getTable().insert();
            insert.forEach((sqlColumn, o) -> statement.value(sqlColumn.getName(), o));
            DBInsert insertResult;
            insertResult = statement.execute().complete();
            if (insertResult == null) {
                throw new TOException("Unable to get insert into database");
            }
            if (primaryKeyValue == null) primaryKeyValue = insertResult.primaryKey();

            for (Map.Entry<Field, SQLTable> extraTables : toObject.getOtherObjects().entrySet()) {
                MultiSecondarySerializer mss = (MultiSecondarySerializer) toConnection.getSecondarySerializer(extraTables.getKey().getType());
                mss.insert(extraTables.getKey().get(value), extraTables.getValue(), primaryKeyValue, extraTables.getKey());
            }
        } catch (IllegalAccessException e) {
            TOConnection.logger.error("Unable to access variable", e);
        } catch (InterruptedException e) {
            TOConnection.logger.error("Unable to get value", e);
            Thread.currentThread().interrupt();
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
        TOValidator.validateClass(tableClass);
        //Now get to work
        List<ColumnBuilder> columns = new ArrayList<>();
        Map<Field, SQLTable> extraTables = new HashMap<>();
        SQLBuilder builder = toConnection.getBuilder();
        String tName = TOUtils.getClassName(tableClass);
        for (Field field : tableClass.getDeclaredFields()) {
            if (field.getAnnotation(TableColumn.class) == null) continue;
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
                    cb.name(TOUtils.getColumnNameByField(field)).setDataType(TOUtils.getColumnType(TOUtils.simpleClass(getPrimaryKeyType(tableClass))));
                    columns.add(cb);
                }
            }
        }
        TableBuilder tableBuilder = builder.createTable().setName(tName);
        columns.forEach(tableBuilder::addColumn);
        SQLTable table = tableBuilder.createTable();
        if (table.getPrimaryColumn() == null) throw new TOException("All TuxORM objects must have a Primary Key");
        objects.put(tableClass, new TOObject(tableClass, table, extraTables));
    }

    public ColumnBuilder createColumn(Field field) {
        TableColumn tableColumn = field.getAnnotation(TableColumn.class);
        ColumnBuilder builder = toConnection.getBuilder().createColumn().name(TOUtils.getColumnNameByField(field)).
                setDataType(TOUtils.getColumnType(simpleClass(field.getType())));
        if (tableColumn.autoIncrement()) builder.autoIncrement();
        if (tableColumn.primary()) builder.primaryKey();
        if (tableColumn.notNull()) builder.notNull();
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
        return builder;
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
                        toResult.getPrimaryTable().getRow().getColumn(object.getColumnForField(field).getName()).orElseThrow(() -> new MissingValueException("Missing Value: " + object.getColumnForField(field).getName())).getAsObject() == null)
                    continue;

                Object value = toResult.getPrimaryTable().getRow().getColumn(object.getColumnForField(field).getName()).orElseThrow(() -> new MissingValueException("Missing Value: " + object.getColumnForField(field).getName())).getAsObject();

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
        if (o == null) return;
        toObject.getTable().delete().where().start(toObject.getTable().getPrimaryColumn().getName(), o).and().execute().queue();
        for (Map.Entry<Field, SQLTable> entry : toObject.getOtherObjects().entrySet()) {
            SQLTable table = entry.getValue();
            table.delete().where().start(PARENT_ID_NAME, o).and().execute().queue();
        }
    }

    public TOObject getToObject(Class<?> type) {
        return objects.get(type);
    }
}

package me.kingtux.tuxorm.daos;

import dev.tuxjsql.core.response.DBRow;
import dev.tuxjsql.core.response.DBSelect;
import dev.tuxjsql.core.sql.SQLTable;
import dev.tuxjsql.core.sql.where.WhereStatement;

import me.kingtux.tuxorm.*;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.MultipleValueSerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSerializerDao<T, I> implements Dao<T, I> {
    private TOObject toObject;
    private DefaultSerializer defaultSerializer;
    private TOConnection connection;

    public DefaultSerializerDao(TOObject toObject, DefaultSerializer defaultSerializer, TOConnection connection) {
        this.toObject = toObject;
        this.defaultSerializer = defaultSerializer;
        this.connection = connection;
    }

    @Override
    public T findByID(I id) {
        return fetchFirst(toObject.getTable().getPrimaryColumn().getName(), id);
    }

    @Override
    public void update(T t) {
        if (t == null) {
            throw new NullPointerException("You cant update null!");
        }
        defaultSerializer.update(t, toObject);
    }

    @Override
    public T create(T t) {
        if (t == null) {
            throw new NullPointerException("You can't insert null into db");
        }
        I id = (I) defaultSerializer.create(t, toObject);

        if (TOConnection.logger.isDebugEnabled())
            connection.getLogger().debug(id.toString());

        return findByID(id);
    }

    @Override
    public List<T> fetchAll() {
        return fetch(null);
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        if (columnName == null || value == null) {
            throw new NullPointerException("Unable to fetch with null values");
        }
        String column = columnName.toLowerCase();
        Field field = toObject.getFieldForColumnName(column);
        if (field == null) {
            throw new NullPointerException(String.format("Unable to find column %s", column));
        }
        Object v = null;
        if (TOUtils.isAnyTypeBasic(field.getType())) {
            v = value;
        } else if (connection.getSecondarySerializer(field.getType()) != null) {
            SecondarySerializer secondarySerializer = connection.getSecondarySerializer(field.getType());
            if (secondarySerializer instanceof SingleSecondarySerializer) {
                v = ((SingleSecondarySerializer) secondarySerializer).getSimplifiedValue(value);
            } else if (secondarySerializer instanceof MultiSecondarySerializer) {
                if (secondarySerializer instanceof MultipleValueSerializer) {
                    column = toObject.getTable().getPrimaryColumn().getName();
                    SQLTable table = toObject.getOtherObjects().get(field);
                    v = ((MultipleValueSerializer) secondarySerializer).contains(value, table);
                } else {
                    WhereStatement whereStatement = ((MultiSecondarySerializer) secondarySerializer).where(value, toObject.getOtherObjects().get(field));
                    DBSelect result = toObject.getOtherObjects().get(field).select().where(whereStatement).execute().complete();
                    v = TOUtils.ids(result, value);
                    column = toObject.getTable().getPrimaryColumn().getName();
                }
            }
        } else {
            v = connection.getPrimaryValue(value);
        }

        if (v instanceof List) {
            List<T> values = new ArrayList<>();
            for (Object object : ((List) v)) {
                values.addAll(fetch(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(object))));
            }
            return values;
        } else {
            return fetch(connection.getBuilder().createWhere().start(column, TOUtils.simplifyObject(v)));
        }
    }

    public List<T> fetch(WhereStatement statement) {
        DBSelect dbRows = toObject.getTable().select().where(statement).execute().complete();
        List<TOResult> results = new ArrayList<>();
        for (DBRow row : dbRows) {
            TableResult tr = new TableResult(row, toObject.getTable());
            Map<Field, TableResult> map = new HashMap<>();
            for (Map.Entry<Field, SQLTable> entry : toObject.getOtherObjects().entrySet()) {
                Object object = TOUtils.simplifyObject(tr.getRow().getRow(toObject.getTable().getPrimaryColumn().getName()).getAsObject());
                DBSelect result = entry.getValue().select().where().start(TOUtils.PARENT_ID_NAME
                        , object).and().execute().complete();
                TableResult subResult =
                        new TableResult(entry.getValue(), result);
                map.put(entry.getKey(), subResult);
            }
            results.add(new TOResult(toObject.getType(), tr, map));
        }
        List<T> list = new ArrayList<>();
        for (TOResult toResult : results) {
            T build = defaultSerializer.build(toObject.getType(), toResult, toObject);
            list.add(build);
        }
        return list;
    }

    @Override
    public void delete(T t) {
        if (t == null) {
            throw new NullPointerException("You cant delete null");
        }
        defaultSerializer.delete(t, toObject);
    }

    @Override
    public void deleteById(I t) {
        delete(findByID(t));
    }

    @Override
    public String getTableName() {
        return toObject.getTable().getName();
    }

    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public TOObject getTOObject() {
        return toObject;
    }

    @Override
    public T refresh(T t) {
        return findByID((I) defaultSerializer.getPrimaryKey(t));
    }
}

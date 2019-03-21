package me.kingtux.tuxorm.daos;

import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;
import me.kingtux.tuxjsql.core.statements.WhereStatement;
import me.kingtux.tuxorm.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultSerializerDao<T, ID> implements Dao<T, ID> {
    private TOObject toObject;
    private DefaultSerializer defaultSerializer;
    private TOConnection connection;

    public DefaultSerializerDao(TOObject toObject, DefaultSerializer defaultSerializer, TOConnection connection) {
        this.toObject = toObject;
        this.defaultSerializer = defaultSerializer;
        this.connection = connection;
    }

    @Override
    public T findByID(ID id) {
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
        ID id = (ID) defaultSerializer.create(t, toObject);
        connection.getLogger().debug(id.toString());
        return findByID(id);
    }

    @Override
    public List<T> fetchAll() {
        return fetch(null);
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        return fetch(TuxJSQL.getSQLBuilder().createWhere().start(columnName, TOUtils.simplifyObject(value)));
    }

    public List<T> fetch(WhereStatement statement) {
        DBResult dbRows = toObject.getTable().select(statement);
        List<TOResult> results = new ArrayList<>();
        for (DBRow row : dbRows) {
            TableResult tr = new TableResult(row, toObject.getTable());
            Map<Field, TableResult> map = new HashMap<>();
            for (Map.Entry<Field, Table> entry : toObject.getOtherObjects().entrySet()) {
                Object object = TOUtils.simplifyObject(tr.getRow().getRowItem(toObject.getTable().getPrimaryColumn().getName()).getAsObject());
                DBResult result = entry.getValue().select(WhereStatement.create().start(TOUtils.PARENT_ID_NAME
                        , object));
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
        defaultSerializer.delete(t, toObject);
    }

    @Override
    public void deleteById(ID t) {
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
        return findByID((ID) defaultSerializer.getPrimaryKey(t));
    }
}

package me.kingtux.tuxorm.daos;

import me.kingtux.tuxjsql.core.sql.where.WhereStatement;

import me.kingtux.tuxorm.*;
import me.kingtux.tuxorm.exceptions.UnableToLocateException;
import me.kingtux.tuxorm.toobjects.TOObject;

import java.util.*;

@SuppressWarnings("unchecked")
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
    public Optional<T> findByID(I id) {
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

        return findByID(id).orElseThrow(() ->
                new UnableToLocateException("Unable to locate something that was just put in")
        );
    }

    @Override
    public List<T> fetchAll() {
        List<T> values = new ArrayList<>();

        List<TOResult> results = DaoUtils.fetch(connection.getBuilder().createWhere(), toObject);
        results.forEach(toResult -> {
            values.add(defaultSerializer.build(toObject.getType(), toResult, toObject));
        });
        return values;
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        List<WhereStatement> wheres = DaoUtils.createWhere(connection.getTuxJSQL(), connection, toObject, columnName, value);
        List<T> values = new ArrayList<>();
        for (WhereStatement whereStatement : wheres) {
            List<TOResult> results = DaoUtils.fetch(whereStatement, toObject);
            results.forEach(toResult -> {
                values.add(defaultSerializer.build(toObject.getType(), toResult, toObject));
            });
        }
        return values;
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
        delete(findByID(t).orElseThrow(() -> new UnableToLocateException("Unable to locate value to delete")));
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
        return findByID((I) defaultSerializer.getPrimaryKey(t)).orElseThrow(() -> new UnableToLocateException("Unable to locate refresh value"));
    }
}

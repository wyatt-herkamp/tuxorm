package me.kingtux.tuxorm.daos;

import dev.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.*;
import me.kingtux.tuxorm.exceptions.UnableToLocateException;
import me.kingtux.tuxorm.toobjects.TOObject;
import me.kingtux.tuxorm.serializers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PrimarySerializerDao<T, I> implements Dao<T, I> {
    private Class<?> type;
    private PrimarySerializer primarySerializer;
    private TOConnection connection;

    public <T> PrimarySerializerDao(Class<T> type, PrimarySerializer primarySerializer, TOConnection toConnection) {
        this.type = type;
        this.primarySerializer = primarySerializer;
        this.connection = toConnection;
    }

    @Override
    public Optional<T> findByID(I id) {
        return fetchFirst(primarySerializer.getTOObject().getTable().getPrimaryColumn().getName(), id);
    }

    @Override
    public void update(T t) {
        primarySerializer.update(t);
    }

    @Override
    public T create(T t) {
        if (t == null) {
            throw new NullPointerException("You can't insert null into db");
        }
        I id = (I) primarySerializer.insert(t);

        if (TOConnection.logger.isDebugEnabled())
            connection.getLogger().debug(id.toString());

        return findByID(id).orElseThrow(() ->
                new UnableToLocateException("Unable to locate something that was just put in")
        );
    }

    @Override
    public List<T> fetchAll() {
        List<T> values = new ArrayList<>();

        List<TOResult> results = DaoUtils.fetch(connection.getBuilder().createWhere(), getTOObject());
        results.forEach(toResult -> {
            values.add((T) primarySerializer.build(toResult));
        });
        return values;
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        List<WhereStatement> wheres = DaoUtils.createWhere(connection.getTuxJSQL(), connection, getTOObject(), columnName, value);
        List<T> values = new ArrayList<>();
        for (WhereStatement whereStatement : wheres) {
            List<TOResult> results = DaoUtils.fetch(whereStatement, getTOObject());
            results.forEach(toResult -> {
                values.add((T) primarySerializer.build(toResult));
            });
        }
        return values;
    }

    @Override
    public void delete(T t) {
        primarySerializer.delete(t);
    }

    @Override
    public void deleteById(I t) {
        delete(findByID(t).orElseThrow(() -> new UnableToLocateException("Unable to locate by id " + t)));
    }

    @Override
    public String getTableName() {
        return primarySerializer.getTableName();
    }

    @Override
    public TOConnection getConnection() {
        return connection;
    }

    @Override
    public TOObject getTOObject() {
        return primarySerializer.getTOObject();
    }

    @Override
    public T refresh(T t) {
        return findByID((I) primarySerializer.getPrimaryKey(t)).orElseThrow(() -> new UnableToLocateException("Unable to locate refresh value"));

    }
}

package me.kingtux.tuxorm.daos;

import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOObject;
import me.kingtux.tuxorm.serializers.PrimarySerializer;

import java.util.List;
//TODO set this up.
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
    public T findByID(I id) {
        return null;
    }

    @Override
    public void update(T t) {

    }

    @Override
    public T create(T t) {
        return null;
    }

    @Override
    public List<T> fetchAll() {
        return null;
    }

    @Override
    public List<T> fetch(String columnName, Object value) {
        return null;
    }

    @Override
    public void delete(T t) {

    }

    @Override
    public void deleteById(I t) {

    }

    @Override
    public String getTableName() {
        return null;
    }

    @Override
    public TOConnection getConnection() {
        return null;
    }

    @Override
    public TOObject getTOObject() {
        return null;
    }

    @Override
    public T refresh(T t) {
        return null;
    }
}

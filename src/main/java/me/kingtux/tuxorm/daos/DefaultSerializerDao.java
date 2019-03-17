package me.kingtux.tuxorm.daos;

import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.TOObject;

import java.util.List;

public class DefaultSerializerDao<T, ID> implements Dao<T,ID> {
    private TOObject toObject;
    @Override
    public T findByID(ID id) {
        return null;
    }

    @Override
    public void update(T t) {

    }

    @Override
    public void create(T t) {

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
    public void deleteById(ID t) {

    }

    @Override
    public void updateOrCreate(T t) {

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
        return toObject;
    }

    @Override
    public T refresh(T t) {
        return t;
    }
}

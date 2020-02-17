package me.kingtux.tuxorm.dao;

import dev.tuxjsql.core.response.DBAction;

public interface Dao<T, I> {
    DBAction<T> create(T t);

    DBAction<T> update(T t);

    DBAction<T> getById(I i);

    DBAction<T> select(String columnName, Object value);
}

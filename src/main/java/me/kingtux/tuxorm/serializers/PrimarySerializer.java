package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxorm.toobjects.TOObject;
import me.kingtux.tuxorm.TOResult;

public interface PrimarySerializer<T, I> {

    I insert(T t);

    void delete(T t);

    void update(T t);

    T build(TOResult toResult);

    TOObject getTOObject();

    String getTableName();

    Class<?> getPrimaryKeyType();

    void createTable();

    Object getPrimaryKey(Object object);



}

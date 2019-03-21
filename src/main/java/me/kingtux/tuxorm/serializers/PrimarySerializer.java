package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxorm.TOObject;
import me.kingtux.tuxorm.TOResult;

public interface PrimarySerializer<T, ID> {


    ID insert(T t);

    void delete(T t);

    void update(T t);

    void build(TOResult toResult);

    TOObject getTOObject();

    String getTableName();

    Class<?> getPrimaryKeyType();

    Object getPrimaryKey(T object);

    void createTable();
}

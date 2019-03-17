package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.WhereStatement;

import java.util.List;

public interface PrimarySerializer<T, ID> {


    ID insert(T t);

    void delete(T t);

    void update(T t);

    /**
     * The One code will get the primary ID
     */
    T fetch(ID id);


    List<T> fetch(WhereStatement statement);

    String getTableName();

    Class<?> getPrimaryKeyType();

    Object getPrimaryKey(T object);

    void createTable();
}

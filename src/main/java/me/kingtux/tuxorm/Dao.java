package me.kingtux.tuxorm;

import java.util.List;

public interface Dao<T, ID> {
    T findByID(ID id);

    void update(T t);

    /**
     * Inserts the item into the system.
     *
     * @param t the value
     */
    void create(T t);

    List<T> fetchAll();

    List<T> fetch(String columnName, String value);

    void updateOrCreate(T t);
}



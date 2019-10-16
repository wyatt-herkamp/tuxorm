package me.kingtux.tuxorm;

import me.kingtux.tuxorm.toobjects.TOObject;

import java.util.List;
import java.util.Optional;

public interface Dao<T, I> {
    Optional<T> findByID(I id);

    void update(T t);

    /**
     * Inserts the item into the system.
     *
     * @param t the value
     * @return a recreation of the object you just sent.
     */
    T create(T t);

    List<T> fetchAll();

    List<T> fetch(String columnName, Object value);

    default Optional<T> fetchFirst(String columnName, Object value) {
        List<T> t = fetch(columnName, value);
        if (t == null || t.isEmpty()) return Optional.empty();
        return Optional.of(t.get(0));
    }

    void delete(T t);

    void deleteById(I t);

    default void updateOrCreate(T t) {
        if (getConnection().getPrimaryValue(t) == null) {
            create(t);
        }else{
            update(t);
        }
    }

    String getTableName();

    TOConnection getConnection();

    TOObject getTOObject();

    T refresh(T t);
}



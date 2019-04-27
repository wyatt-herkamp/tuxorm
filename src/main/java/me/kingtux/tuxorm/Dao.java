package me.kingtux.tuxorm;

import java.util.List;

public interface Dao<T, ID> {
    T findByID(ID id);

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

    default T fetchFirst(String columnName, Object value) {
        List<T> t = fetch(columnName, value);
        if (t == null || t.size()==0) return null;
        return t.get(0);
    }

    void delete(T t);

    void deleteById(ID t);

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



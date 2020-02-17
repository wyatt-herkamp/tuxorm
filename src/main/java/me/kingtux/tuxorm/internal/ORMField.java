package me.kingtux.tuxorm.internal;

import dev.tuxjsql.core.sql.SQLTable;

import java.lang.reflect.Field;
import java.util.Optional;

public interface ORMField {


    Field getField();

    SQLTable getTable();

    ORMFieldType fieldType();

    Object getValue(Object instance);

    void setValue(Object instance, Object value);

    /**
     * Returns the value that will be put in the database.
     *
     * @param instance the instance of the value
     * @return the better value
     */
    Object insert(Object instance);

    void insert(Object value, Object parentKey);

    Object getFieldResult(ORMResult result);
}

package me.kingtux.tuxorm.internal;

import dev.tuxjsql.core.sql.SQLTable;

import java.lang.reflect.Field;
import java.util.Optional;

public interface ORMField {


    Field getField();

    SQLTable getTable();

    Object getValue(Object instance);

    void setValue(Object instance);

    void insert(Object value);

    void insert(Object value, Object parentKey);

    Object getFieldResult(ORMResult result);
}

package me.kingtux.tuxorm.internal;

import dev.tuxjsql.core.sql.SQLTable;

import java.util.List;

public interface ORMObject {


    List<ORMField> getFields();

    String name();

    Class<?> getObjectClass();

    SQLTable getTable();
}

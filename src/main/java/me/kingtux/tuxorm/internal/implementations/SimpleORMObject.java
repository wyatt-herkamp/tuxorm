package me.kingtux.tuxorm.internal.implementations;

import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.internal.ORMObject;

import java.util.ArrayList;
import java.util.List;

public class SimpleORMObject implements ORMObject {
    private String name;
    private Class<?> clazz;
    private List<ORMField> ormFields;
    private SQLTable table;

    public SimpleORMObject(String name, Class<?> clazz, List<ORMField> ormFields, SQLTable table) {
        this.name = name;
        this.clazz = clazz;
        this.ormFields = ormFields;
        this.table = table;
    }

    @Override
    public List<ORMField> getFields() {
        return new ArrayList<>(ormFields);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> getObjectClass() {
        return clazz;
    }

    @Override
    public SQLTable getTable() {
        return table;
    }
}

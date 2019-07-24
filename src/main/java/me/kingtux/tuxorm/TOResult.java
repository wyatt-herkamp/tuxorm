package me.kingtux.tuxorm;

import java.lang.reflect.Field;
import java.util.Map;

public class TOResult {
    private Class<?> clazz;
    private TableResult primaryTable;
    private Map<Field, TableResult> extraTables;

    public TOResult(Class<?> clazz, TableResult primaryTable, Map<Field, TableResult> extraTables) {
        this.clazz = clazz;
        this.primaryTable = primaryTable;
        this.extraTables = extraTables;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public TableResult getPrimaryTable() {
        return primaryTable;
    }

    public Map<Field, TableResult> getExtraTables() {
        return extraTables;
    }
}

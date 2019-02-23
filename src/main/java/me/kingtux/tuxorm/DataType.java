package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.ColumnType;
import me.kingtux.tuxjsql.core.CommonDataTypes;

public enum DataType {
    TEXT(CommonDataTypes.TEXT),
    INT(CommonDataTypes.INT),
    DOUBLE(CommonDataTypes.DOUBLE),
    DEFAULT(null),
    BOOLEAN(CommonDataTypes.TEXT),
    BIGINT(CommonDataTypes.BIGINT);

    private ColumnType columnType;

    DataType(ColumnType columnType) {
        this.columnType = columnType;
    }

    public ColumnType getColumnType(Class<?> type) {
        if (columnType == null) {
            if (type == String.class) {
                return TEXT.columnType;
            } else if (type == int.class || type == Integer.class) {
                return INT.columnType;
            } else if (type == double.class || type == Double.class) {
                return DOUBLE.columnType;
            } else if (type == long.class || type == Long.class) {
                return BIGINT.columnType;
            }else if(type == boolean.class || type == Boolean.class){
                return BOOLEAN.columnType;
            }
        }
        return columnType;
    }
}

package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.DataType;

public enum DataTypes {
    TEXT(CommonDataTypes.TEXT),
    INT(CommonDataTypes.INT),
    DOUBLE(CommonDataTypes.DOUBLE),
    DEFAULT(null),
    BOOLEAN(CommonDataTypes.TEXT),
    BIGINT(CommonDataTypes.BIGINT);

    private DataType columnType;

    DataTypes(DataType columnType) {
        this.columnType = columnType;
    }

    public DataType getColumnType(Class<?> type) {
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

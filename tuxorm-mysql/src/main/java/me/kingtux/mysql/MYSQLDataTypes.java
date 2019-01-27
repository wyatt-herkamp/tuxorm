package me.kingtux.mysql;

import me.kingtux.tuxorm.datatypes.CommonDataTypes;
import me.kingtux.tuxorm.datatypes.DataType;

public enum MYSQLDataTypes implements DataType {
    TEXT("TEXT", CommonDataTypes.STRING),
    INT("INT", CommonDataTypes.INT);

    MYSQLDataTypes(String type) {
        this.type = type;
    }

    MYSQLDataTypes(String type, CommonDataTypes cmt) {
        this.type = type;
        this.cmt = cmt;
    }

    private String type;
    private CommonDataTypes cmt;

    @Override
    public String getType() {
        return type;
    }

    public static DataType getType(CommonDataTypes cmt) {
        for (MYSQLDataTypes mysqlDataTypes : values()) {
            if (mysqlDataTypes.cmt == cmt) {
                return mysqlDataTypes;
            }
        }
        return null;
    }
}

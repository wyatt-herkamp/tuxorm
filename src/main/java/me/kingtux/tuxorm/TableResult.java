package me.kingtux.tuxorm;


import dev.tuxjsql.core.response.DBRow;
import dev.tuxjsql.core.response.DBSelect;
import dev.tuxjsql.core.sql.SQLTable;

public class TableResult {
    private DBRow row;
    private SQLTable table;
    private DBSelect result;

    public TableResult(DBRow result, SQLTable table) {
        this.row = result;
        this.table = table;
    }

    public TableResult(SQLTable table, DBSelect result) {
        this.table = table;
        this.result = result;
    }

    public DBSelect getResult() {
        return result;
    }

    public DBRow getRow() {
        return row;
    }

    public SQLTable getTable() {
        return table;
    }
}

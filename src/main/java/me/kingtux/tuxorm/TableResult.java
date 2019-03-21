package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.Table;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;

public class TableResult {
    private DBRow row;
    private Table table;
    private DBResult result;

    public TableResult(DBRow result, Table table) {
        this.row = result;
        this.table = table;
    }

    public TableResult(Table table, DBResult result) {
        this.table = table;
        this.result = result;
    }

    public DBResult getResult() {
        return result;
    }

    public DBRow getRow() {
        return row;
    }

    public Table getTable() {
        return table;
    }
}

import me.kingtux.mysql.MYSQLTableBuilder;
import me.kingtux.tuxorm.ORMConnection;
import me.kingtux.tuxorm.builders.QueryBuilder;
import me.kingtux.tuxorm.builders.TableBuilder;

import java.io.IOException;
import java.sql.Connection;

public class TestConnection implements ORMConnection {

    @Override
    public QueryBuilder getQueryBuilder() {
        return null;
    }



    @Override
    public TableBuilder createTableBuilder() {
        return new MYSQLTableBuilder();
    }

    @Override
    public void executeSimpleQuery(String buildQuery) {

    }


    @Override
    public boolean tableExists(String name) {
        return false;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void close() throws IOException {

    }
}

package me.kingtux.mysql;

import me.kingtux.tuxorm.ORMConnection;
import me.kingtux.tuxorm.builders.QueryBuilder;
import me.kingtux.tuxorm.builders.TableBuilder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MYSQLORMConnection implements ORMConnection {
    private Connection connection;

    public MYSQLORMConnection(Connection connection) {
        this.connection = connection;
    }

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
        try {
            connection.createStatement().execute(buildQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean tableExists(String name) {
        try {
            return tableExist(connection, name);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    private static boolean tableExist(Connection conn, String tableName) throws SQLException {
        boolean tExists = false;
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            while (rs.next()) {
                String tName = rs.getString("TABLE_NAME");
                if (tName != null && tName.equals(tableName)) {
                    tExists = true;
                    break;
                }
            }
        }
        return tExists;
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

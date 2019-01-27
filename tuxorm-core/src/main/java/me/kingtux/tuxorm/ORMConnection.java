package me.kingtux.tuxorm;

import me.kingtux.tuxorm.builders.QueryBuilder;
import me.kingtux.tuxorm.builders.TableBuilder;

import java.io.Closeable;
import java.sql.Connection;

public interface ORMConnection extends Closeable {


    QueryBuilder getQueryBuilder();

    TableBuilder createTableBuilder();


    void executeSimpleQuery(String buildQuery);


    boolean tableExists(String name);


    Connection getConnection();
}

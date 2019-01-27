package me.kingtux.tuxorm.builders;

import me.kingtux.tuxorm.dbo2.DBField;

/**
 * This is a table builder!
 *
 * @author KingTux
 */
public interface TableBuilder {


    TableBuilder withName(String name);

    TableBuilder addColumn(DBField dbField);

    String buildQuery();
}

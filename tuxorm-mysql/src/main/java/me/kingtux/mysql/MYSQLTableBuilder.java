package me.kingtux.mysql;


import me.kingtux.tuxorm.builders.TableBuilder;
import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.DatabaseRules;

import java.util.ArrayList;
import java.util.List;

public class MYSQLTableBuilder implements TableBuilder {
    private String name;
    private List<DBField> columns = new ArrayList<>();
    public final static String QUERY_FORMAT = "CREATE TABLE %1$s (%2$s)";

    @Override
    public TableBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public TableBuilder addColumn(me.kingtux.tuxorm.dbo2.DBField dbField) {
        columns.add(dbField);
        return this;
    }

    @Override
    public String buildQuery() {
        return String.format(MYSQLTableBuilder.QUERY_FORMAT, name, formatColumns()) + ";";
    }

    private String formatColumns() {
        StringBuilder builder = new StringBuilder();
        boolean notFirst = false;
        for (DBField dbField : columns) {
            if (!notFirst) {
                notFirst = true;
                builder.append(",");
            }
            if (!(dbField instanceof DatabaseRules)) continue;
            builder.append(buildColumn((DatabaseRules) dbField));

        }
        return builder.toString();
    }

    private String buildColumn(DatabaseRules dbField) {
        StringBuilder builder = new StringBuilder(dbField.getName() + " " + MYSQLDataTypes.getType(dbField.getDataType()).getType());
        if (dbField.isPrimary()) {
            builder.append(" PRIMARY KEY ");
        }
        if (dbField.autoIncrement()) {
            builder.append(" AUTO INCREMENT ");
        }
        if (!dbField.isNullable()) {
            builder.append(" NOT NULL ");
        }
        return builder.toString().trim().replaceAll(" +", " ");
    }
}

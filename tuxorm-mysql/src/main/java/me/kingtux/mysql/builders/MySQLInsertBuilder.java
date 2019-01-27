package me.kingtux.mysql.builders;

import me.kingtux.mysql.MySQLUtils;
import me.kingtux.tuxorm.NameGen;
import me.kingtux.tuxorm.ORMConnection;
import me.kingtux.tuxorm.ORMUtils;
import me.kingtux.tuxorm.builders.InsertBuilder;
import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.DatabaseRules;

import java.sql.SQLException;
import java.util.Map;


public class MySQLInsertBuilder implements InsertBuilder {
    private Object object;
    public static final String format = "INSERT INTO %1$s (%2$s) VALUES (%3$s) ";
    private ORMConnection ormConnection;

    @Override
    public InsertBuilder forObject(Object o) {
        this.object = o;
        return this;
    }

    private String build() {
        String tableName = NameGen.getTableName(object.getClass());
        StringBuilder itemsToAdd = new StringBuilder();
        int numberOfValues = 0;
        for (DBField dbField : ORMUtils.getDBFields(object.getClass())) {
            if (dbField instanceof DatabaseRules) {
                itemsToAdd.append(((DatabaseRules) dbField).getName()).append(" ");
                numberOfValues++;
            }
        }
        StringBuilder preparedStatmentMarks = new StringBuilder();
        for (int i = 0; i < numberOfValues; i++) {
            if (i != 0) {
                preparedStatmentMarks.append(",");
            }
            preparedStatmentMarks.append("?");
        }
        return String.format(format, tableName, itemsToAdd.toString(), preparedStatmentMarks.toString());
    }

    @Override
    public <T> T execute() {
        String builtThing = build();
        int id = 0;
        Object primaryKeyValue = ORMUtils.getPrimaryKeyValue(object);
        Map<Integer, Object> objectMap = MySQLUtils.sortForInsertPreparedStatement(builtThing, ORMUtils.getTheValues(object));
        try {
            ORMUtils.query(ormConnection, builtThing, objectMap);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        buildForCollections(id, object);
        return null;
    }

    private void buildForCollections(int object, Object o) {
    }
}

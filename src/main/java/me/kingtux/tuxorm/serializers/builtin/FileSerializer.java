package me.kingtux.tuxorm.serializers.builtin;


import me.kingtux.tuxjsql.basic.sql.BasicDataTypes;
import me.kingtux.tuxjsql.core.builders.ColumnBuilder;
import me.kingtux.tuxjsql.core.sql.SQLColumn;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.io.File;

public class FileSerializer implements SingleSecondarySerializer<File, String> {
    private TOConnection connection;

    public FileSerializer(TOConnection toConnection) {
        this.connection = toConnection;
    }

    @Override
    public String getSimplifiedValue(File o) {
        return o.getPath();
    }

    @Override
    public File buildFromSimplifiedValue(String value) {
        return new File(value);
    }

    @Override
    public ColumnBuilder createColumn(String name) {
        return connection.getBuilder().createColumn().name(name).setDataType(BasicDataTypes.TEXT);
    }
}

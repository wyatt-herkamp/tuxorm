package me.kingtux.tuxorm.serializers.builtin;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.CommonDataTypes;
import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;

import java.io.File;

public class FileSerializer implements SingleSecondarySerializer<File, String> {
    @Override
    public String getSimplifiedValue(File o) {
        return o.getPath();
    }

    @Override
    public File buildFromSimplifiedValue(String value) {
        return new File(value);
    }

    @Override
    public Column createColumn(String name) {
        return TuxJSQL.getBuilder().createColumn(name, CommonDataTypes.TEXT);
    }
}

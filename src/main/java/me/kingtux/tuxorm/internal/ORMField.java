package me.kingtux.tuxorm.internal;

import dev.tuxjsql.core.response.DBColumnItem;
import dev.tuxjsql.core.sql.SQLColumn;
import dev.tuxjsql.core.sql.SQLTable;
import me.kingtux.tuxorm.serializer.Serializer;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ORMField {

    Serializer getSerializer();

    Field getField();

    String getColumnName();

    Map<SQLColumn, Object> parse(Object object);

    Object parse(List<DBColumnItem> dbColumnItemList);

    void set(Object object, Object instance);

    Object get(Object instance);
}

package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Table;

import java.util.List;

public interface MultipleValueSerializer {

    List<Object> contains(Object o, Table table);
}

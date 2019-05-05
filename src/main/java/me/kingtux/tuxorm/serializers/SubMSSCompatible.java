package me.kingtux.tuxorm.serializers;

import me.kingtux.tuxjsql.core.Column;
import me.kingtux.tuxjsql.core.result.DBResult;
import me.kingtux.tuxjsql.core.result.DBRow;

import java.util.List;
import java.util.Map;

public interface SubMSSCompatible<T> {

    /**
     * These columns are used for other MultiSecondarySerializer.
     *
     * @return
     */
    List<Column> getColumns();

    Map<Column, Object> getValues(T t);

    T minorBuild(DBRow dbRows);
}

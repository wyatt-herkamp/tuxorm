package me.kingtux.tuxorm.serializer;

import dev.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.internal.ORMResult;

public interface DefaultSerializer {


    ORMObject createORMObject(Class<?> clazz);

    ORMResult getORMResult(WhereStatement whereStatement);

    <T> T createObject(ORMResult ormResult);

    void setTuxORM(TuxORM tuxORM);

    TuxORM getTuxORM();
}

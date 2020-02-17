package me.kingtux.tuxorm.serializer.implementations;

import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.internal.ORMResult;
import me.kingtux.tuxorm.internal.implementations.SimpleORMField;
import me.kingtux.tuxorm.serializer.DefaultSerializer;
import me.kingtux.tuxorm.utils.DataTypeUtils;
import me.kingtux.tuxorm.utils.TuxORMUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class BasicDefaultSerializer implements DefaultSerializer {
    private TuxORM tuxORM;

    @Override
    public ORMObject createORMObject(Class<?> clazz) {
        ORMObject ormObject = null;
        List<SimpleORMField> ormFieldList = new ArrayList<>();
        for (Field field : TuxORMUtils.getFields(clazz)) {
            SQLDataType dataType = DataTypeUtils.getDataType(field, tuxORM.getTuxJSQL().getBuilder().name());
        }

        return ormObject;
    }

    @Override
    public ORMResult getORMResult(WhereStatement whereStatement) {
        return null;
    }

    @Override
    public <T> T createObject(ORMResult ormResult) {
        return null;
    }

    @Override
    public void setTuxORM(TuxORM tuxORM) {
        if (this.tuxORM != null) throw new IllegalStateException("TuxORM has already been set. So square up thots.");
        this.tuxORM = tuxORM;
    }

    @Override
    public TuxORM getTuxORM() {
        return tuxORM;
    }
}

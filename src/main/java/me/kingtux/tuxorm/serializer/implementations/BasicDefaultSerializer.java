package me.kingtux.tuxorm.serializer.implementations;

import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.SQLTable;
import dev.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.annotations.DatabateTable;
import me.kingtux.tuxorm.internal.MultiTypeInternalORMField;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.internal.ORMResult;
import me.kingtux.tuxorm.internal.implementations.SimpleInternalORMField;
import me.kingtux.tuxorm.internal.implementations.SimpleORMField;
import me.kingtux.tuxorm.internal.implementations.SimpleORMObject;
import me.kingtux.tuxorm.serializer.DefaultSerializer;
import me.kingtux.tuxorm.serializer.PrimarySerializer;
import me.kingtux.tuxorm.serializer.SecondarySerializer;
import me.kingtux.tuxorm.serializer.SingleTypeSecondarySerializer;
import me.kingtux.tuxorm.utils.DataTypeUtils;
import me.kingtux.tuxorm.utils.TuxORMUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicDefaultSerializer implements DefaultSerializer {
    private TuxORM tuxORM;

    @Override
    public boolean isORMObjectCompatible(Class<?> clazz) {
        return clazz.getAnnotation(DatabateTable.class) != null;
    }

    @Override
    public Object getPrimaryKey(Object object) {
        return null;
    }

    @Override
    public ORMObject createORMObject(Class<?> clazz) {
        ORMObject ormObject = null;
        List<ORMField> ormFieldList = new ArrayList<>();
        for (Field field : TuxORMUtils.getFields(clazz)) {
            ORMField simpleORMField;
            SQLDataType dataType = DataTypeUtils.getDataType(field, tuxORM.getTuxJSQL().getBuilder().name());
            if (dataType != null) {
            } else {
                Optional<SecondarySerializer> secondarySerializer = tuxORM.getSerializerManager().getSecondarySerializer(field.getType());
                if (secondarySerializer.isEmpty()) {
                    Optional<PrimarySerializer> primarySerializer = tuxORM.getSerializerManager().getPrimarySerializer(field.getType());
                    if (primarySerializer.isPresent()) {

                    } else {
                        if (isORMObjectCompatible(clazz)) {

                        } else {
                            throw new MissingSerializerException();
                        }
                    }
                } else {
                    SecondarySerializer secondary = secondarySerializer.get();
                    if (secondary instanceof SingleTypeSecondarySerializer) {

                    }
                }
            }
            ormFieldList.add(simpleORMField);
        }
        ormObject = new SimpleORMObject(TuxORMUtils.getTableName(clazz), clazz, ormFieldList, createTable(ormFieldList));

        return ormObject;
    }

    private SQLTable createTable(List<SimpleORMField> ormFieldList) {

    }

    private SQLTable createTableForSubObject(Field field) {

        return null;
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

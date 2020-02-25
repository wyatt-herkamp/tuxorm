package me.kingtux.tuxorm.serializer.implementations;

import dev.tuxjsql.core.builders.TableBuilder;
import dev.tuxjsql.core.sql.SQLDataType;
import dev.tuxjsql.core.sql.SQLTable;
import dev.tuxjsql.core.sql.where.WhereStatement;
import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.annotations.Table;
import me.kingtux.tuxorm.internal.ORMField;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.internal.ORMResult;
import me.kingtux.tuxorm.internal.implementations.BasicInternalORMField;
import me.kingtux.tuxorm.internal.implementations.ReferenceORMField;

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
        return clazz.getAnnotation(Table.class) != null;
    }

    @Override
    public Object getPrimaryKey(Object object) {
        return null;
    }

    @Override
    public ORMObject createORMObject(Class<?> clazz) {
        List<ORMField> ormFieldList = new ArrayList<>();
        for (Field field : TuxORMUtils.getFields(clazz)) {
            ORMField simpleORMField = null;
            SQLDataType dataType = DataTypeUtils.getDataType(field, tuxORM.getTuxJSQL().getBuilder().name());
            if (dataType != null) {
                simpleORMField = new BasicInternalORMField(field, dataType);
            } else {
                Optional<SecondarySerializer> secondarySerializer = tuxORM.getSerializerManager().getSecondarySerializer(field.getType());
                if (secondarySerializer.isEmpty()) {
                    Optional<PrimarySerializer> primarySerializer = tuxORM.getSerializerManager().getPrimarySerializer(field.getType());
                    if (primarySerializer.isPresent()) {
                        simpleORMField = new ReferenceORMField(field, primarySerializer.get());
                    } else {
                        if (isORMObjectCompatible(clazz)) {
                            simpleORMField = new ReferenceORMField(field, this);
                        } else {
                            throw new MissingSerializerException();
                        }
                    }
                } else {
                    SecondarySerializer secondary = secondarySerializer.get();
                    if (secondary instanceof SingleTypeSecondarySerializer) {
                        throw new RuntimeException("Unsupported Featue currently");
                    }
                }
            }
            ormFieldList.add(simpleORMField);
        }
        return createTable(ormFieldList, TuxORMUtils.getTableName(clazz), clazz);
    }

    private ORMObject createTable(List<ORMField> ormFieldList, String name, Class<?> clazz) {
        TableBuilder tableBuilder = tuxORM.getTuxJSQL().createTable();
        tableBuilder.setName(name);
        List<ORMField> ormFields = new ArrayList<>();

        for (ORMField field : ormFields) {

        }
        return new SimpleORMObject(name, clazz, ormFields, tableBuilder.createTable());

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

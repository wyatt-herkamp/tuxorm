package me.kingtux.tuxorm;

import me.kingtux.tuxjsql.core.builders.SQLBuilder;
import me.kingtux.tuxorm.daos.DefaultSerializerDao;
import me.kingtux.tuxorm.serializers.MultiSecondarySerializer;
import me.kingtux.tuxorm.serializers.PrimarySerializer;
import me.kingtux.tuxorm.serializers.SecondarySerializer;
import me.kingtux.tuxorm.serializers.SingleSecondarySerializer;
import me.kingtux.tuxorm.serializers.builtin.FileSerializer;
import me.kingtux.tuxorm.serializers.builtin.ListSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TOConnection {
    private SQLBuilder builder;
    private Map<Class, PrimarySerializer> primarySerializers = new HashMap<>();
    private Map<Class<?>, SecondarySerializer> secondarySerializers = new HashMap<>();
    private DefaultSerializer defaultSerializer;
    protected Logger logger = LoggerFactory.getLogger("TuxORM");

    public TOConnection(SQLBuilder builder) {
        secondarySerializers.put(List.class, new ListSerializer(this));
        registerSecondarySerializer(File.class, new FileSerializer());
        defaultSerializer = new DefaultSerializer(this);
        this.builder = builder;
    }

    public SQLBuilder getBuilder() {
        return builder;
    }

    public Class<?> getPrimaryType(Class<?> firstType) {
        for (Map.Entry<Class, PrimarySerializer> ecp : primarySerializers.entrySet()) {
            if (firstType.isAssignableFrom(ecp.getKey())) {
                return ecp.getValue().getPrimaryKeyType();
            }
        }
        return defaultSerializer.getPrimaryKeyType(firstType);
    }

    public Object getPrimaryValue(Object object) {
        for (Map.Entry<Class, PrimarySerializer> ecp : primarySerializers.entrySet()) {
            if (ecp.getKey().isInstance(object)) {
                return ecp.getValue().getPrimaryKey(object);
            }
        }
        return defaultSerializer.getPrimaryKey(object);
    }

    public <T> Object quickGet(Class<T> type, Object id) {
        Dao<T, Object> dao = createDao(type);
        return dao.findByID(id);
    }

    public <T, ID> Dao<T, ID> createDao(Class<T> type) {
        registerClass(type);
        Dao<T, ID> dao;
        if (getPrimarySerializer(type) == null) {
            dao = new DefaultSerializerDao<T,ID>(defaultSerializer.getToObject(type), defaultSerializer,this);
        } else {
            //Handle Later
            dao = null;
        }
        return dao;
    }

    public <T, ID> Dao<T, ID> createDao(T type) {
        return (Dao<T, ID>) createDao(type.getClass());
    }


    public void registerClass(Class<?> type) {
        if (getPrimarySerializer(type) == null) {
            defaultSerializer.createTable(type);
        } else {
            getPrimarySerializer(type).createTable();
        }
    }

    public SecondarySerializer getSecondarySerializer(Class<?> type) {
        for (Map.Entry<Class<?>, SecondarySerializer> ecp : secondarySerializers.entrySet()) {
            if (type.isAssignableFrom(ecp.getKey())) {
                return ecp.getValue();
            }
        }
        return null;
    }

    public void registerSecondarySerializer(Class<?> type, SecondarySerializer secondarySerializer) {
        if (!(secondarySerializer instanceof SingleSecondarySerializer) && !(secondarySerializer instanceof MultiSecondarySerializer)) {
            logger.error("Failed To Register SecondarySerializer", new IllegalArgumentException(secondarySerializers.getClass().getName() + " Must be an instance of  a SingleSecondarySerializer  or MultiSecondarySerializer"));
            return;
        }
        Map.Entry<Class<?>, SecondarySerializer> entry = null;
        for (Map.Entry<Class<?>, SecondarySerializer> ecp : secondarySerializers.entrySet()) {
            if (type.isAssignableFrom(ecp.getKey())) {
                entry = ecp;
            }
        }
        if (entry != null) return;
        secondarySerializers.put(type, secondarySerializer);
    }

    public PrimarySerializer getPrimarySerializer(Class<?> type) {
        for (Map.Entry<Class, PrimarySerializer> ecp : primarySerializers.entrySet()) {
            if (type.isAssignableFrom(ecp.getKey())) {
                return ecp.getValue();
            }
        }
        return null;
    }

    public Object quickInsert(Object value) {
        return value;
    }

    public Logger getLogger() {
        return logger;
    }
}

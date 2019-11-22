package me.kingtux.tuxorm;

import dev.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.config.ORMConfig;
import me.kingtux.tuxorm.dao.Dao;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.serializer.DefaultSerializer;
import me.kingtux.tuxorm.serializer.DefaultSerializerUtils;
import me.kingtux.tuxorm.serializer.SerializerManager;
import me.kingtux.tuxorm.utils.ORMObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class TuxORM {
    private TuxJSQL tuxJSQL;
    private ORMConfig config;
    private List<ORMObject> registeredObjects = new ArrayList<>();
    private SerializerManager serializerManager = new SerializerManager(this);
    public static final Logger LOGGER = LoggerFactory.getLogger(TuxORM.class);

    public TuxORM(TuxJSQL tuxJSQL) {
        this(tuxJSQL, ORMConfig.getDefaultConfig());
    }

    public TuxORM(TuxJSQL tuxJSQL, ORMConfig config) {
        this(tuxJSQL, config, null);
    }

    public TuxORM(TuxJSQL tuxJSQL, ORMConfig config, SerializerManager serializerManager) {
        this.tuxJSQL = tuxJSQL;
        this.config = config;
        if (serializerManager != null) this.serializerManager = serializerManager;
    }

    public <T, I> Dao<T, I> createDao(Class<?> clazz) {
        ORMObject ormObject;
        if (ORMObjectUtils.containsByType(clazz, registeredObjects)) {
            ormObject = ORMObjectUtils.getObjectByType(clazz, registeredObjects);
        } else {
            registerObject(clazz);
            ormObject = ORMObjectUtils.getObjectByType(clazz, registeredObjects);
        }
        return createDao(ormObject);
    }

    public <T, I> Dao<T, I> createDao(ORMObject object) {
        return null;
    }

    public void registerObject(Class<?> clazz) {
        if (ORMObjectUtils.containsByType(clazz, registeredObjects)) return;
        ORMObject object = createORMObject(clazz);
        ORMObjectUtils.setupDatebase(object, this);
        registeredObjects.add(object);
    }

    private ORMObject createORMObject(Class<?> clazz) {
        return getDefaultSerializer().createORMObject(clazz);
    }

    public DefaultSerializer getDefaultSerializer() {
        return serializerManager.getDefaultSerializer();
    }

    public SerializerManager getSerializerManager() {
        return serializerManager;
    }
}

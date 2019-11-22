package me.kingtux.tuxorm.serializer;

import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.serializer.PrimarySerializer;
import me.kingtux.tuxorm.serializer.SecondarySerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This is more a Object to hold other objects and manage those Objects.
 */
public final class SerializerManager {
    private Map<Class, PrimarySerializer> primarySerializers = new HashMap<>();
    private Map<Class, SecondarySerializer> secondarySerializers = new HashMap<>();
    private DefaultSerializer defaultSerializer;
    private TuxORM tuxORM;

    public SerializerManager(TuxORM tuxORM) {
        this(DefaultSerializerUtils.createDefaultSerializer(), tuxORM);
    }

    public SerializerManager(DefaultSerializer defaultSerializer, TuxORM tuxORM) {
        this.defaultSerializer = defaultSerializer;
        this.tuxORM = tuxORM;
    }

    public void registerSerializer(Class clazz, Object object) {
        if (object instanceof PrimarySerializer) {
            registerPrimarySerializer(clazz, (PrimarySerializer) object);
        } else if (object instanceof SecondarySerializer) {
            registerSecondarySerializer(clazz, (SecondarySerializer) object);
        } else {
            TuxORM.LOGGER.error("Unable to register serializer for " + clazz.getName(), new IllegalArgumentException("The value must either be a PrimarySerializer or SecondarySerializer"));
        }
    }

    private void registerPrimarySerializer(Class clazz, PrimarySerializer object) {
        primarySerializers.putIfAbsent(clazz, object);
    }

    private void registerSecondarySerializer(Class clazz, SecondarySerializer object) {
        secondarySerializers.putIfAbsent(clazz, object);
    }

    public Map<Class, PrimarySerializer> getPrimarySerializers() {
        return primarySerializers;
    }

    public Map<Class, SecondarySerializer> getSecondarySerializers() {
        return secondarySerializers;
    }

    public Optional<PrimarySerializer> getPrimarySerializer(Class clazz) {
        return Optional.ofNullable(primarySerializers.get(clazz));
    }

    public Optional<SecondarySerializer> getSecondarySerializer(Class clazz) {
        return Optional.ofNullable(secondarySerializers.get(clazz));

    }

    public DefaultSerializer getDefaultSerializer() {
        return defaultSerializer;
    }

    public TuxORM getTuxORM() {
        return tuxORM;
    }
}

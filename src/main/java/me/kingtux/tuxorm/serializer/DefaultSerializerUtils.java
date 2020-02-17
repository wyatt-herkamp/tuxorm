package me.kingtux.tuxorm.serializer;

import me.kingtux.tuxorm.serializer.implementations.BasicDefaultSerializer;

public class DefaultSerializerUtils {
    public static DefaultSerializer createDefaultSerializer() {
        return new BasicDefaultSerializer();
    }
}



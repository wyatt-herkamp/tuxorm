package me.kingtux.tuxorm.utils;

import me.kingtux.tuxorm.serializer.PrimarySerializer;
import me.kingtux.tuxorm.serializer.SecondarySerializer;

import java.util.HashMap;
import java.util.Map;

public class SerializerUtils {
    private Map<Class, PrimarySerializer> primarySerializers = new HashMap<>();
    private Map<Class, SecondarySerializer> secondarySerializers = new HashMap<>();


}

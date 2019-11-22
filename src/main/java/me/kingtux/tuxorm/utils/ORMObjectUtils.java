package me.kingtux.tuxorm.utils;

import me.kingtux.tuxorm.TuxORM;
import me.kingtux.tuxorm.internal.ORMObject;

import java.util.List;

public class ORMObjectUtils {
    public static boolean containsByType(Class<?> type, List<ORMObject> objects) {
        return getObjectByType(type, objects) != null;
    }

    public static ORMObject getObjectByType(Class<?> type, List<ORMObject> objects) {
        for (ORMObject object : objects) {
            if (object.getObjectClass().equals(type)) return object;
        }
        return null;
    }

    public static void setupDatebase(ORMObject object, TuxORM tuxORM) {

    }
}

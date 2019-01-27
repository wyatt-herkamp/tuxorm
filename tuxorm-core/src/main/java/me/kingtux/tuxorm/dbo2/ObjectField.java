package me.kingtux.tuxorm.dbo2;

import java.lang.reflect.Field;

/**
 * This means its connected to an Object or Class
 */
public interface ObjectField {

    Class<?> getClazz();

    Field getField();
}

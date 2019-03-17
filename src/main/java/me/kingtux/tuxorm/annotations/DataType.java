package me.kingtux.tuxorm.annotations;

import me.kingtux.tuxjsql.core.CommonDataTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataType {

    CommonDataTypes type();
}

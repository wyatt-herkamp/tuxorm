package me.kingtux.tuxorm.annotations;

import me.kingtux.tuxjsql.basic.sql.BasicDataTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DataType {

    BasicDataTypes type();
}

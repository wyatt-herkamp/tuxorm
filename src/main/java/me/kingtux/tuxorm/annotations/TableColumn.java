package me.kingtux.tuxorm.annotations;

import me.kingtux.tuxorm.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TableColumn {
    String name() default "";

    boolean autoIncrement() default false;

    boolean unique() default false;

    boolean nullable() default false;

    boolean primary() default false;

    DataType dataType() default DataType.DEFAULT;
}

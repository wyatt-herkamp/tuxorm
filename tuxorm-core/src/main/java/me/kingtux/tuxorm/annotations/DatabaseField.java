package me.kingtux.tuxorm.annotations;

import me.kingtux.tuxorm.datatypes.CommonDataTypes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DatabaseField {

    String name() default "";

    CommonDataTypes dataType() default CommonDataTypes.UNKOWN;

    boolean nullable() default false;
}

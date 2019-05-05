package me.kingtux.tuxorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
/**
 * Use this on Fields you want in the table.
 *
 * @author KingTux
 */
public @interface TableColumn {
    /**
     * Specify a custom culumn name
     *
     * @return column name
     */
    String name() default "";

    /**
     * If true the column will be auto increment
     *
     * @return if auto increment
     */
    boolean autoIncrement() default false;

    /**
     * If the column is unique
     *
     * @return if unique
     */
    boolean unique() default false;

    /**
     * Specify true if you want it to fail if the value is null
     *
     * @return if notnull
     */
    boolean notNull() default false;

    /**
     * If you want to mark this value as primary.
     *
     * @return if primary
     */
    boolean primary() default false;

    /**
     * if this is set to true. We are going to set the default for items based on what its set to by default. Via inline set or default constructor
     *
     * @return is default
     */
    boolean useDefault() default false;
}

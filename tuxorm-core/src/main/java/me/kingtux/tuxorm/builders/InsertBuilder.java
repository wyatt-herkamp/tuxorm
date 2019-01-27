package me.kingtux.tuxorm.builders;

public interface InsertBuilder {


    InsertBuilder forObject(Object o);

    /**
     * Returns the object with the id set
     *
     * @param <T> idk
     * @return the object if the id set!
     */
    <T> T execute();


}

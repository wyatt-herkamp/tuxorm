package me.kingtux.tuxorm.dao;

import me.kingtux.tuxorm.internal.ORMObject;

public interface DaoManager {

    <T, I> Dao<T, I> createDao(ORMObject object);
}

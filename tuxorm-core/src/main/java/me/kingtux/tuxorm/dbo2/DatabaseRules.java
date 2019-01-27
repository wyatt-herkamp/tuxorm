package me.kingtux.tuxorm.dbo2;

import me.kingtux.tuxorm.datatypes.CommonDataTypes;

public interface DatabaseRules extends Nameable {

    boolean isPrimary();

    boolean autoIncrement();

    boolean isNullable();

    CommonDataTypes getDataType();

}

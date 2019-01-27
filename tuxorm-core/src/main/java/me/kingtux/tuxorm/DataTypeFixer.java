package me.kingtux.tuxorm;

import me.kingtux.tuxorm.datatypes.CommonDataTypes;
import me.kingtux.tuxorm.datatypes.DataType;
@FunctionalInterface
public interface DataTypeFixer {
    DataType getType(CommonDataTypes dataTypes);
}



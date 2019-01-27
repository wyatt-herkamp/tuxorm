package me.kingtux.tuxorm.dbo2.items;

import me.kingtux.tuxorm.datatypes.CommonDataTypes;
import me.kingtux.tuxorm.dbo2.DBField;
import me.kingtux.tuxorm.dbo2.DatabaseRules;
import me.kingtux.tuxorm.dbo2.Nameable;

/**
 * This is a field inside a collection table;
 * Mainly used when making tables
 */
public class CollectionField implements DBField, DatabaseRules, Nameable {
    private String name;
    private boolean primary;
    private CommonDataTypes commonDataTypes;

    public CollectionField(String name, boolean primary) {
        this.name = name;
        this.primary = primary;
    }

    public CollectionField(String name) {
        this(name, false);
    }

    public CollectionField(String name, CommonDataTypes byType) {
        this(name, false);
        commonDataTypes = byType;
    }

    @Override
    public boolean isPrimary() {
        return primary;
    }

    @Override
    public boolean autoIncrement() {
        return primary;
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public CommonDataTypes getDataType() {
        return commonDataTypes;
    }

    @Override
    public String getName() {
        return name;
    }
}

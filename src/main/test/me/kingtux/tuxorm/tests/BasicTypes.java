package me.kingtux.tuxorm.tests;

import me.kingtux.tuxorm.annotations.TableColumn;

public class BasicTypes {
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn()
    private int anInt;
    @TableColumn()
    private double anDouble;
    @TableColumn()
    private String anString;
    @TableColumn()
    private boolean randomBoolean;

    public BasicTypes(int anInt, double anDouble, String anString, boolean randomBoolean) {
        this.anInt = anInt;
        this.anDouble = anDouble;
        this.anString = anString;
        this.randomBoolean = randomBoolean;
    }
}

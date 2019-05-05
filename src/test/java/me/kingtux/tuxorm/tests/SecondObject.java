package me.kingtux.tuxorm.tests;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

@DBTable
public class SecondObject  {
    @TableColumn(autoIncrement = true, primary = true)
    private long id;
    @TableColumn
    private long createdOn = System.currentTimeMillis();
    @TableColumn
    private long updatedLast = System.currentTimeMillis();
    @TableColumn
    private String name;

    public SecondObject() {
    }

    public SecondObject(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package me.kingtux.tuxorm.tests.objects;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.Arrays;
import java.util.List;

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
    @TableColumn
    private List<Item> is = Arrays.asList(new Item("1",1), new Item("2", 2));

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

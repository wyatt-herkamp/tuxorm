package me.kingtux.tuxorm.tests;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.List;

@DBTable
public class BasicLists {
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn()
    private List<Integer> intergerList;
    @TableColumn()
    private List<Double> doubleList;
    @TableColumn()
    private List<Boolean> booleanList;
    @TableColumn()
    private List<String> stringList;

}

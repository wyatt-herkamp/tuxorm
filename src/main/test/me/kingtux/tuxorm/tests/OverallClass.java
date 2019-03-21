package me.kingtux.tuxorm.tests;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.Arrays;
import java.util.List;

@DBTable
public class OverallClass {
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn
    private String name;
    @TableColumn
    private List<Long> longs = Arrays.asList(1L, 2L, 3L);

    public OverallClass(String name) {
        this.name = name;
    }

    public OverallClass(String name, List<Long> longs) {
        this.name = name;
        this.longs = longs;
    }

    public OverallClass() {
    }

    public String getName() {
        return name;
    }
}

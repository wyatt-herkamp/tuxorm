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
    @TableColumn
    private boolean value = false;

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

    public void setName(String cool_guy) {
        name = cool_guy;
    }

    public List<Long> getLongs() {
        return longs;
    }

    public int getId() {
        return id;
    }

    public void setLongs(List<Long> longs) {
        this.longs = longs;
    }
}

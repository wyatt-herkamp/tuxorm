package me.kingtux.tuxorm.tests;

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@DBTable
public class OverallClass {
    @TableColumn(autoIncrement = true, primary = true)
    private long id;
    @TableColumn
    private long createdOn = System.currentTimeMillis();
    @TableColumn
    private long updatedLast = System.currentTimeMillis();
    @TableColumn(useDefault = true)
    private String name = "GAY";
    @TableColumn
    private SecondObject object;
    @TableColumn
    private List<Long> longs = Arrays.asList(1L, 2L, 3L);
    @TableColumn
    private boolean value = false;
    @TableColumn
    private File file = new File("test.txt");
    public OverallClass(String name) {
        this.name = name;
    }

    public OverallClass(String name, SecondObject object, List<Long> longs) {
        this.name = name;
        this.object = object;
        this.longs = longs;
    }

    public OverallClass() {
    }

    public void setValue(boolean value) {
        this.value = value;
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

    public void setLongs(List<Long> longs) {
        this.longs = longs;
    }

    @Override
    public String toString() {
        return "OverallClass{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", longs=" + longs +
                ", value=" + value +
                '}';
    }
}

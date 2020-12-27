package me.kingtux.tuxorm.tests.objects;

import me.kingtux.tuxorm.BasicLoggingObject;
import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DBTable
public class OverallClass extends BasicLoggingObject {

    @TableColumn()
    private String name = "GAY";
    @TableColumn
    private SecondObject object;
    @TableColumn
    private List<Long> longs = Arrays.asList(1L, 2L, 3L);
    @TableColumn
    private boolean value = false;
    @TableColumn
    private File file = new File("test.txt");
    @TableColumn
    private Map<Integer, Item> items = new HashMap<>();
    @TableColumn
    private Item item = new Item("bob", 1234);
    @TableColumn
    private TestEnum testEnum;

    public OverallClass(String name) {
        this.name = name;
    }

    public OverallClass(String name, SecondObject object, List<Long> longs) {
        this.name = name;
        this.object = object;
        this.longs = longs;
    }

    public OverallClass(String name, SecondObject object, List<Long> longs, TestEnum testEnum) {
        this.name = name;
        this.object = object;
        this.longs = longs;
        this.testEnum = testEnum;
    }

    public OverallClass() {
    }

    public Item put(Integer integer, Item s) {
        return items.put(integer, s);
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

    public SecondObject getObject() {
        return object;
    }

    public TestEnum getTestEnum() {
        return testEnum;
    }

    public void setTestEnum(TestEnum testEnum) {
        this.testEnum = testEnum;
    }

    public File getFile() {
        return file;
    }
}

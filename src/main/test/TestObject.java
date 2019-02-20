import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.Arrays;
import java.util.List;

@DBTable
public class TestObject {
    @TableColumn
    private String name;
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn()
    private List<ForeignItem> flist;
    @TableColumn
    private ForeignItem coolItem;

    public TestObject() {

    }


    public TestObject(String name, List<ForeignItem> flist, ForeignItem coolItem) {
        this.name = name;
        this.flist = flist;
        this.coolItem = coolItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<ForeignItem> getFlist() {
        return flist;
    }

    public void setFlist(List<ForeignItem> flist) {
        this.flist = flist;
    }

    public ForeignItem getCoolItem() {
        return coolItem;
    }

    public void setCoolItem(ForeignItem coolItem) {
        this.coolItem = coolItem;
    }
}

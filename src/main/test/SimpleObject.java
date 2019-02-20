import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.Arrays;
import java.util.List;

@DBTable
public class SimpleObject {

    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn
    private String name;
    @TableColumn()
    private List<String> soItems = Arrays.asList("bob","CAt");
    @TableColumn
    private ForeignItem foreignItem;
    public SimpleObject(String name, ForeignItem foreignItem) {
        this.name = name;
        this.foreignItem = foreignItem;
    }
    public SimpleObject(){

    }

    public String getName() {
return name;    }
}

import me.kingtux.tuxorm.annotations.DBTable;
import me.kingtux.tuxorm.annotations.TableColumn;
@DBTable
public class ForeignItem {
    @TableColumn(primary = true, autoIncrement = true)
    private int id;
    @TableColumn
    private String coolName;

    public ForeignItem(String coolName) {
        this.coolName = coolName;
    }
    public ForeignItem(){

    }
}

import me.kingtux.tuxorm.annotations.DatabaseField;
import me.kingtux.tuxorm.annotations.DatabaseTable;
import me.kingtux.tuxorm.annotations.Id;
import me.kingtux.tuxorm.annotations.ORMCollection;

import java.util.List;

@DatabaseTable(name = "test")
public class TestClass {
    @Id
    private int id;
    @DatabaseField
    private String test;
    @ORMCollection(isBasic = true)
    private List<String> testBList;
}

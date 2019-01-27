import me.kingtux.tuxorm.ORMUtils;

public class TableBuilderTester {
    public static void main(String[] s) {
        ORMUtils.createTable(new TestConnection(), TestClass.class);
    }
}

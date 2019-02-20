import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.ORMConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {

        Properties properties = new Properties();
        String path = System.getProperty("user.home") + "/mysql.properties";
        System.out.println(path);
        properties.load(new FileInputStream(new File(path)));
        ORMConnection ormConnection = ORMConnection.build(TuxJSQL.Type.MYSQL, properties);
        ormConnection.registerTable(TestObject.class);
        ormConnection.registerTable(ForeignItem.class);
     //   ormConnection.registerTable(SimpleObject.class);


        ForeignItem coolItem = new ForeignItem("Coool!");
        Dao<ForeignItem, Integer> fdao = ormConnection.createDAO(coolItem);
        fdao.create(coolItem);
        //
        TestObject testObject = new TestObject("test", Arrays.asList(fdao.findByID(1)), fdao.findByID(1));
        Dao<TestObject, Integer> todao = ormConnection.createDAO(testObject);
        //todao.create(testObject);
        testObject = todao.findByID(1);
        System.out.println(testObject.getName());
        testObject.setName("YEET");
        todao.update(testObject);
    }
}

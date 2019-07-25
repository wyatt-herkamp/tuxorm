package me.kingtux.tuxorm.tests;

import dev.tuxjsql.core.TuxJSQL;
import dev.tuxjsql.core.TuxJSQLBuilder;
import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;
import me.kingtux.tuxorm.tests.objects.Item;
import me.kingtux.tuxorm.tests.objects.OverallClass;
import me.kingtux.tuxorm.tests.objects.SecondObject;
import org.junit.jupiter.api.Test;
import sun.applet.Main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestMain {
    private Properties properties = new Properties();

    public TestMain() {
        properties.setProperty("db.type", "dev.tuxjsql.sqlite.SQLiteBuilder");
        properties.setProperty("db.file", "db.db");
    }


    public static void main(String[] args) {
        new TestMain().baseTests();
    }

    @Test
    public void baseTests() {
        new File("db.db").deleteOnExit();
        TOConnection connection = new TOConnection(TuxJSQLBuilder.create(properties));
        connection.registerSecondarySerializer(Item.class, new TestSubMMS(connection));
        connection.registerClass(OverallClass.class);
        //Create Daos
        Dao<SecondObject, Long> sdao = connection.createDao(SecondObject.class);
        Dao<OverallClass, Long> dao = connection.createDao(OverallClass.class);
        assertNotNull(sdao);
        assertNotNull(dao);

        //Test Object Creation
        OverallClass clazz = new OverallClass("Welcome", new SecondObject("COOL MAN"), Arrays.asList(4L, 6L));
        clazz.put(15, new Item("Hey", 134));
        clazz.put(45, new Item("HeyBobby", 135346));
        clazz = dao.create(clazz);
        assertNotNull(clazz);
        assertNotNull(clazz.getObject());
        //Test Updating
        clazz.setName("Cool Guy");
        clazz.setLongs(Arrays.asList(3L, 6L, 8L, 9L));
        dao.update(clazz);
        assertNotNull(dao.fetchFirst("name", "Cool Guy"));
        //Test Custom Fetching
        assertNotNull(dao.fetchFirst("file", new File("test.txt")));
        assertNotNull(dao.fetchFirst("object", sdao.findByID(1L)));
        assertNotNull(dao.fetchFirst("longs", 3L));
        assertNotNull(dao.fetchFirst("items", 45));
        assertNotNull(dao.fetchFirst("item", new Item("bob", 1234)));
    }

    @Test
    public void mysqlTest() {
        Properties properties = getLocalProperties();
        properties.setProperty("db.type", "dev.tuxjsql.mysql.MysqlBuilder");

        TOConnection connection = new TOConnection(TuxJSQLBuilder.create(properties));
        connection.registerSecondarySerializer(Item.class, new TestSubMMS(connection));
        connection.registerClass(OverallClass.class);
        //Create Daos
        Dao<SecondObject, Long> sdao = connection.createDao(SecondObject.class);
        Dao<OverallClass, Long> dao = connection.createDao(OverallClass.class);
        assertNotNull(sdao);
        assertNotNull(dao);

        //Test Object Creation
        OverallClass clazz = new OverallClass("Welcome", new SecondObject("COOL MAN"), Arrays.asList(4L, 6L));
        clazz.put(15, new Item("Hey", 134));
        clazz.put(45, new Item("HeyBobby", 135346));
        clazz = dao.create(clazz);
        assertNotNull(clazz);
        assertNotNull(clazz.getObject());
        //Test Updating
        clazz.setName("Cool Guy");
        clazz.setLongs(Arrays.asList(3L, 6L, 8L, 9L));
        dao.update(clazz);
        assertNotNull(dao.fetchFirst("name", "Cool Guy"));
        //Test Custom Fetching
        assertNotNull(dao.fetchFirst("file", new File("test.txt")));
        assertNotNull(dao.fetchFirst("object", sdao.findByID(1L)));
        assertNotNull(dao.fetchFirst("longs", 3L));
        assertNotNull(dao.fetchFirst("items", 45));
        assertNotNull(dao.fetchFirst("item", new Item("bob", 1234)));
    }

    private Properties getLocalProperties() {
        Properties properties = new Properties();
        File file = new File(System.getProperty("user.home"), "mysql.properties");
        if (!file.exists()) {
            throw new RuntimeException("Please configure a mysql.properties in your home directory");
        }
        try {
            properties.load(new FileReader(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

}

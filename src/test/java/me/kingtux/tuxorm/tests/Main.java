package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class Main {
    private static TOConnection connection;

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("db.type", "SQLITE");
        properties.setProperty("db.file", "db.db");
        //TuxJSQL.setDatasource(properties);
        connection = new TOConnection(TuxJSQL.setup(properties));
        connection.registerSecondarySerializer(Item.class,new TestSubMMS(connection));
        connection.registerClass(OverallClass.class);

        Dao<SecondObject, Long> sdao = connection.createDao(SecondObject.class);
        Dao<OverallClass, Long> dao = connection.createDao(OverallClass.class);
        OverallClass clazz = new OverallClass("Welcome",  new SecondObject("COOL MAN"), Arrays.asList(4L, 6L));
        clazz.put(15,"GAY");
        clazz = dao.create(clazz);
        System.out.println(clazz.toString());
        clazz.setName("Cool Guy");
        clazz.setLongs(Arrays.asList(3L, 6L, 8L, 9L));
        dao.update(clazz);
        System.out.println(clazz.toString());
        clazz = dao.fetchFirst("name", "Cool Guy");
       System.out.println(clazz.toString());

        OverallClass s = dao.fetchFirst("file", new File("test.txt"));
        if (s == null) {
            System.out.println("Failed TO by FIle");
        }
        OverallClass st = dao.fetchFirst("object", sdao.findByID(1L));
        if (st == null) {
            System.out.println("Failed TO by object reference");
        }
        //dao.delete(clazz);
    }

}

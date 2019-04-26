package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static TOConnection connection;

    public static void main(String[] args) throws IOException {
        //Properties properties = new Properties();
//        String path = System.getProperty("user.home") + "/mysql.properties";
  //      System.out.println("Path to mysql settings: " + path);
    //    properties.load(new FileInputStream(new File(path)));
        //TuxJSQL.setBuilder(TuxJSQL.Type.SQLITE);
        Properties properties = new Properties();
        properties.setProperty("db.type", "SQLITE");
        properties.setProperty("db.file", "db.db");
        //TuxJSQL.setDatasource(properties);
        connection = new TOConnection(TuxJSQL.setup(properties));
        connection.registerClass(OverallClass.class);
        Dao<OverallClass, Integer> dao = connection.createDao(OverallClass.class);

        OverallClass clazz = new OverallClass("Welcome");
        clazz = dao.create(clazz);
        System.out.println(clazz.toString());
        clazz.setName("Cool Guy");
        clazz.setLongs(Arrays.asList(3L, 6L, 8L, 9L));
        dao.update(clazz);
        System.out.println(clazz.toString());
        clazz = dao.fetchFirst("name", "Welcome");
       System.out.println(clazz.toString());
        dao.delete(clazz);
    }

}

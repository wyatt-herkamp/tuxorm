package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.Dao;
import me.kingtux.tuxorm.TOConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static TOConnection connection;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        Properties properties = new Properties();
        String path = System.getProperty("user.home") + "/mysql.properties";
        System.out.println("Path to mysql settings: " + path);
        properties.load(new FileInputStream(new File(path)));
        TuxJSQL.setBuilder(TuxJSQL.Type.MYSQL);
        TuxJSQL.setDatasource(properties);
        connection = new TOConnection();
        connection.registerClass(OverallClass.class);
        Dao<OverallClass, Integer> dao = connection.createDao(OverallClass.class);
        dao.create(new OverallClass("gay"));
        System.out.println(dao.findByID(15).getName());
    }

}

package me.kingtux.tuxorm.tests;

import me.kingtux.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.ORMConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static ORMConnection connection;
    private static Scanner scanner;

    public static void main(String[] args) throws IOException {
        scanner = new Scanner(System.in);
        Properties properties = new Properties();
        String path = System.getProperty("user.home") + "/mysql.properties";
        System.out.println("Path to mysql settings: " + path);
        properties.load(new FileInputStream(new File(path)));
        connection = ORMConnection.build(TuxJSQL.Type.MYSQL, properties);
        System.out.print("Please select a thing to test!");
        System.out.print("Basic Types 1 \n" +
                "BasicLists 2 \n" +
                "Foregin Field 3\n" +
                "Foreign Lists 4\n");
        int thingToTest = scanner.nextInt();
        if (thingToTest == 1) {
            basicTypes();
        }
    }

    public static void basicTypes() {
        connection.registerTable(BasicTypes.class);


        System.out.print("Delete Table");
        if (scanner.next().equalsIgnoreCase("yes")) {
            try {
                TuxJSQL.getConnection().createStatement().execute("DROP TABLE basictypes;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void basicListTypes() {
        connection.registerTable(BasicLists.class);


        System.out.print("Delete Table");
        if (scanner.next().equalsIgnoreCase("yes")) {
            try {
                TuxJSQL.getConnection().createStatement().execute("DROP TABLE basiclists;");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

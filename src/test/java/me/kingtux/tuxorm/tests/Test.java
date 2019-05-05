package me.kingtux.tuxorm.tests;

public class Test {

    public static void main(String[] args) {
        Object[] array  = {"1", 1};
        test(1, 35, array);
    }

    public static void test(Object... o ){
        System.out.println(o.length);
    }
}

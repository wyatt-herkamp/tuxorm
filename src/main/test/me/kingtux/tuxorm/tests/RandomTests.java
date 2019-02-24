package me.kingtux.tuxorm.tests;

public class RandomTests {
    public static void main(String[] args) {
        Object o = TestEnum.ONE;

        if (o.getClass().isEnum()) {
            System.out.println(((Enum) o).name());
        }
    }
}

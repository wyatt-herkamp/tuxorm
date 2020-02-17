package me.kingtux.tuxorm.config;

public class ORMConfig {
    private int executorSize = 1;

    private ORMConfig() {
    }

    public static ORMConfig createConfig() {
        return new ORMConfig();
    }

    public static ORMConfig getDefaultConfig() {
        return createConfig();
    }

    public int getExecutorSize() {
        return executorSize;
    }
}

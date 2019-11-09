package me.kingtux.tuxorm.config;

public class ORMConfig {
    private ORMConfig() {
    }

    public static ORMConfig createConfig() {
        return new ORMConfig();
    }

    public static ORMConfig getDefaultConfig() {
        return createConfig();
    }
}

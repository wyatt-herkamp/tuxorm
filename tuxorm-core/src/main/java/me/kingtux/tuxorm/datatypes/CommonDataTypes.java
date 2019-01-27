package me.kingtux.tuxorm.datatypes;

public enum CommonDataTypes implements DataType {

    UNKOWN("UNKOWN"),
    OBJECT_ID("OBJECT_ID"),
    STRING("STRING", String.class),
    INT("INT", int.class);

    CommonDataTypes(String unkown) {
        type = unkown;
    }

    CommonDataTypes(String unkown, Class<?>... supportedTypes) {
        type = unkown;
        this.supportedTypes = supportedTypes;
    }

    private String type;
    private Class<?>[] supportedTypes;

    public Class<?>[] getSupportedTypes() {
        return supportedTypes;
    }

    public static CommonDataTypes getByType(Class<?> type) {
        for (CommonDataTypes cdt : values()) {
            if (cdt.supportedTypes == null) continue;
            for (Class<?> cdtType : cdt.supportedTypes) {
                if (cdtType == type) {
                    return cdt;
                }
            }
        }
        return null;
    }

    @Override
    public String getType() {
        return type;
    }
}

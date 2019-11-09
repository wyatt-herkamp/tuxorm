package me.kingtux.tuxorm;

import dev.tuxjsql.core.TuxJSQL;
import me.kingtux.tuxorm.config.ORMConfig;
import me.kingtux.tuxorm.internal.ORMObject;
import me.kingtux.tuxorm.serializer.DefaultSerializer;
import me.kingtux.tuxorm.serializer.DefaultSerializerUtils;

import java.util.ArrayList;
import java.util.List;

public class TuxORM {
    private TuxJSQL tuxJSQL;
    private ORMConfig config;
    private List<ORMObject> registeredObjects = new ArrayList<>();
    private DefaultSerializer defaultSerializer;

    public TuxORM(TuxJSQL tuxJSQL) {
        this(tuxJSQL, ORMConfig.getDefaultConfig());
    }

    public TuxORM(TuxJSQL tuxJSQL, ORMConfig config) {
        this(tuxJSQL, config, DefaultSerializerUtils.createDefaultSerializer());
    }

    public TuxORM(TuxJSQL tuxJSQL, ORMConfig config, DefaultSerializer defaultSerializer) {
        this.tuxJSQL = tuxJSQL;
        this.config = config;
        this.defaultSerializer = defaultSerializer;
    }
}

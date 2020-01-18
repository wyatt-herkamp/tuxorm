package me.kingtux.tuxorm;

import me.kingtux.tuxorm.annotations.TableColumn;

public class SimpleObject {
    @TableColumn(primary = true, autoIncrement = true)
    protected long id;
    @TableColumn
    protected long createdOn = System.currentTimeMillis();
    @TableColumn
    protected long updatedOn = System.currentTimeMillis();
}

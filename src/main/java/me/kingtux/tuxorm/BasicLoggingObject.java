package me.kingtux.tuxorm;

import me.kingtux.tuxorm.annotations.TableColumn;

import java.util.Objects;

/**
 * This is a basic Object that you can extend to make development easier.
 * <p>
 * It contains a long id. that is auto increment and primary key.
 * <p>
 * and a long createdOn and a long UpdatedOn
 */
public class BasicLoggingObject {
    @TableColumn(primary = true, autoIncrement = true)
    protected long id;
    @TableColumn
    protected long createdOn = System.currentTimeMillis();
    @TableColumn
    protected long updatedOn = System.currentTimeMillis();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BasicLoggingObject that = (BasicLoggingObject) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "SimpleObject{" +
                "id=" + id +
                ", createdOn=" + createdOn +
                ", updatedOn=" + updatedOn +
                '}';
    }

    public long getId() {
        return id;
    }

    public long getCreatedOn() {
        return createdOn;
    }

    public long getUpdatedOn() {
        return updatedOn;
    }
}

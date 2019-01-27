package me.kingtux.tuxorm.builders;

import me.kingtux.tuxorm.builders.where.WhereBuilder;

public interface QueryBuilder {

    InsertBuilder createInsert();

    UpdateBuilder createUpdate();

    DeleteBuilder createDelete();

    SelectBuilder createSelect();

    WhereBuilder createWhere();

}

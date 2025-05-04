package com.pythongong.community.infras.database;

import java.util.List;

import org.jooq.Condition;
import org.jooq.UpdatableRecord;
import org.jooq.impl.TableImpl;

public record SqlQuery<T extends UpdatableRecord<T>>(
                TableImpl<T> table, List<Condition> conditions) {

}

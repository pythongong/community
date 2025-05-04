package com.pythongong.community.infras.database;

import java.util.List;

import org.springframework.r2dbc.core.DatabaseClient;

import com.pythongong.community.infras.exception.CommunityException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveSql {

    private final DatabaseClient databaseClient;

    private StringBuilder sql;

    private boolean hasWhere;

    public ReactiveSql(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
        this.sql = new StringBuilder();
        this.hasWhere = false;
    }

    public Mono<Void> instert(String table, List<RowRecord> rowRecords) {
        this.sql.append("instert into ").append(table);
        combineStrs(rowRecords.stream().map((rowRecord) -> rowRecord.column()).toList());
        values(rowRecords.stream().map((rowRecord) -> rowRecord.val()).toList());
        String sqlStr = this.sql.toString();
        return databaseClient.sql(sqlStr).mapValue(Integer.class).one().flatMap((count) -> {
            if (count != 1) {
                return Mono.error(new CommunityException("Insert: {" + sqlStr + "} failed"));
            }
            return Mono.empty();
        });
    }

    public Mono<Integer> count() {
        this.sql = new StringBuilder("select count(*)").append(this.sql);
        return databaseClient.sql(this.sql.toString()).mapValue(Integer.class).one();
    }

    private void values(List<String> values) {
        this.sql.append(" VALUES");
        combineStrs(values);
    }

    public ReactiveSql from(String table) {
        this.sql.append(" from ").append(table);
        return this;
    }

    public ReactiveSql eq(String column, String val) {
        initCondition();
        this.sql.append(column).append(" = ").append(val);
        return this;
    }

    private void initCondition() {
        if (!hasWhere) {
            addWhere();
            hasWhere = true;
        } else {
            addAnd();
        }
    }

    private void addAnd() {
        this.sql.append(" and ");
    }

    private void addWhere() {
        this.sql.append(" where ");
    }

    private void combineStrs(List<String> values) {
        this.sql.append("(");

        for (int i = 0; i < values.size() - 1; i++) {
            this.sql.append(values.get(i)).append(", ");
        }
        this.sql.append(values.get(values.size() - 1));
        this.sql.append(") ");
    }

}

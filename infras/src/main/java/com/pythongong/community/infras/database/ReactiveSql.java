package com.pythongong.community.infras.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.r2dbc.core.DatabaseClient;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveSql {

        private final DatabaseClient databaseClient;

        private final List<String> whereClauses;

        private final Map<String, Object> bindMap;

        public ReactiveSql(DatabaseClient databaseClient) {
                this.whereClauses = new ArrayList<>();
                this.bindMap = new HashMap<>();
                this.databaseClient = databaseClient;
        }

        public Mono<Long> insert(String tableName, List<SqlColumn> dataToBind) {
                String columns = dataToBind.stream()
                                .map(sqlColumn -> sqlColumn.column())
                                .collect(Collectors.joining(", "));

                String placeholders = dataToBind.stream()
                                .map(sqlColumn -> ":" + sqlColumn.column()) // Using named placeholders
                                .collect(Collectors.joining(", "));

                String sql = String.format("INSERT INTO %s (%s) VALUES (%s)", tableName, columns, placeholders);

                log.info("sql: {}", sql);
                // Convert List<KeyValue> to Map<String, Object>
                dataToBind.forEach(sqlColumn -> bindMap.put(sqlColumn.column(), sqlColumn.val()));

                return databaseClient.sql(sql)
                                .bindValues(bindMap)
                                .fetch()
                                .rowsUpdated();
        }

        public Mono<Long> count(String tableName) {
                String sql = String.format("SELECT count(*) FROM %s WHERE %s ", tableName,
                                whereClauses.stream().collect(Collectors.joining(" AND ")));
                log.info("sql: {}", sql);
                return databaseClient.sql(sql)
                                .bindValues(bindMap)
                                .fetch()
                                .one()
                                .map(row -> {
                                        return (Long) row.get("count(*)");
                                });
        }

        public ReactiveSql eq(String column, Object val) {
                this.whereClauses.add(column + " = :" + column);
                this.bindMap.put(column, val);
                return this;
        }

}

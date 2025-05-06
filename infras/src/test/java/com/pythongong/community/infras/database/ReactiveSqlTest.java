package com.pythongong.community.infras.database;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.FetchSpec;
import reactor.core.publisher.Mono;

@SuppressWarnings(value = { "rawtypes", "unchecked" })
@ExtendWith(MockitoExtension.class)
class ReactiveSqlTest {

        private ReactiveSql reactiveSql;

        @Mock
        private DatabaseClient.GenericExecuteSpec executeSpec;

        @Mock
        private FetchSpec fetchSpec;

        @Mock
        private DatabaseClient databaseClient;

        @BeforeEach
        void setUp() {
                reactiveSql = new ReactiveSql(databaseClient);
        }

        @Test
        void testInsert() {
                // Given
                List<SqlColumn> records = Arrays.asList(
                                new SqlColumn("name", "'John'"),
                                new SqlColumn("age", "25"));
                when(databaseClient.sql(anyString())).thenReturn(executeSpec);
                when(executeSpec.bindValues(anyMap())).thenReturn(executeSpec);
                when(executeSpec.fetch()).thenReturn(fetchSpec);
                when(fetchSpec.rowsUpdated()).thenReturn(Mono.just(1L));

                // When & Then
                reactiveSql.insert("users", records).subscribe(num -> {
                        assertTrue(num == 1);
                });

        }

        @Test
        void testCount() {
                // Given
                reactiveSql.eq("name", "'John'").eq("age", 25);
                when(databaseClient.sql(anyString())).thenReturn(executeSpec);
                when(executeSpec.bindValues(anyMap())).thenReturn(executeSpec);
                when(executeSpec.fetch()).thenReturn(fetchSpec);
                when(fetchSpec.one()).thenReturn(Mono.just(Map.of("count(*)", 5L)));

                // When
                Mono<Long> result = reactiveSql.count("users");

                // Then
                result.subscribe(num -> assertTrue(num == 5));
        }
}
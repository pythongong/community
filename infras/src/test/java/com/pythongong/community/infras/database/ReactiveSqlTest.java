package com.pythongong.community.infras.database;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import org.springframework.test.util.ReflectionTestUtils;

import com.pythongong.community.infras.exception.CommunityException;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SuppressWarnings(value = { "rawtypes", "unchecked" })
@ExtendWith(MockitoExtension.class)
class ReactiveSqlTest {

        private ReactiveSql reactiveSql;

        @Mock
        private DatabaseClient.GenericExecuteSpec executeSpec;

        @Mock
        private RowsFetchSpec rowsFetchSpec;

        @Mock
        private DatabaseClient databaseClient;

        @BeforeEach
        void setUp() {
                reactiveSql = new ReactiveSql(databaseClient);
        }

        @Test
        void testInsert() {
                // Given
                List<RowRecord> records = Arrays.asList(
                                new RowRecord("name", "'John'"),
                                new RowRecord("age", "25"));
                when(databaseClient.sql(anyString())).thenReturn(executeSpec);
                when(executeSpec.mapValue(Integer.class)).thenReturn(rowsFetchSpec);
                when(rowsFetchSpec.one()).thenReturn(Mono.just(1));

                // When & Then
                assertDoesNotThrow(() -> reactiveSql.instert("users", records));

                assertEquals("instert into users(name, age)  VALUES('John', 25) ",
                                ReflectionTestUtils.getField(reactiveSql, "sql").toString());

        }

        @Test
        void testCount() {
                // Given
                reactiveSql.from("users").eq("name", "'John'").eq("age", "25");
                when(databaseClient.sql(anyString())).thenReturn(executeSpec);
                when(executeSpec.mapValue(Integer.class)).thenReturn(rowsFetchSpec);
                when(rowsFetchSpec.one()).thenReturn(Mono.just(5));

                // When
                Mono<Integer> result = reactiveSql.count();

                // Then
                StepVerifier.create(result)
                                .expectNext(5)
                                .verifyComplete();

                assertEquals("select count(*) from users where name = 'John' and age = 25",
                                ReflectionTestUtils.getField(reactiveSql, "sql").toString());
        }

        @Test
        void testInsertShouldThrowErrorWhenCountIsNotOne() {
                // Given
                List<RowRecord> records = Arrays.asList(
                                new RowRecord("name", "'John'"),
                                new RowRecord("age", "25"));
                when(databaseClient.sql(anyString())).thenReturn(executeSpec);
                when(executeSpec.mapValue(Integer.class)).thenReturn(rowsFetchSpec);
                when(rowsFetchSpec.one()).thenReturn(Mono.just(0));
                // When & Then
                StepVerifier.create(reactiveSql.instert("users", records))
                                .expectError(CommunityException.class)
                                .verify();
        }
}
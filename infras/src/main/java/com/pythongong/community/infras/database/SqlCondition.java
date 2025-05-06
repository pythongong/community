package com.pythongong.community.infras.database;

public record SqlCondition(
        String operator,
        Object val) {

}

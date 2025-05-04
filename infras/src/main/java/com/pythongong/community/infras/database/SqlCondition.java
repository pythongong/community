package com.pythongong.community.infras.database;

import lombok.Builder;

@Builder
public record SqlCondition(
                String column,
                String operator,
                String val) {

}

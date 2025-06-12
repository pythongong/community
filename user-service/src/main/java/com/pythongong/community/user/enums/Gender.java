package com.pythongong.community.user.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE(0),
    FEMALE(1);

    private final int value;

    Gender(int value) {
        this.value = value;
    }

    public static int getValue(String gender) {
        for (Gender g : Gender.values()) {
            if (g.name().equals(gender)) {
                return g.value;
            }
        }
        return -1;
    }
}

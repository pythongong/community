package com.pythongong.community.user.enums;

import lombok.Getter;

@Getter
public enum Gender {
    MALE((short) 0),
    FEMALE((short) 1);

    private final short value;

    Gender(short value) {
        this.value = value;
    }

    public static short getValue(String gender) {
        for (Gender g : Gender.values()) {
            if (g.name().equals(gender)) {
                return g.value;
            }
        }
        return -1;
    }
}

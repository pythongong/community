package com.pythongong.community.user.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommunityUser {
    /**
     * Unique auto-incrementing integer identifier for each user
     */
    private Integer id;

    /**
     * Timestamp of user record creation
     */
    private LocalDateTime createTime;

    /**
     * Timestamp of last user record update
     */
    private LocalDateTime updateTime;

    /**
     * Unique username (1-20 characters)
     */
    private String userName;

    /**
     * User password (8-20 characters, store encrypted value)
     */
    private String userPassword;

    /**
     * User gender (0: male, 1: female)
     */
    private Integer gender;

    /**
     * Type of the user (REGULAR, OFFICIAL, KOL)
     */
    private String userType;
}

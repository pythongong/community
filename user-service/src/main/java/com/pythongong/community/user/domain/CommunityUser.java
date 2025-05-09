package com.pythongong.community.user.domain;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@Accessors(chain = true)
@Table("community_user")
public class CommunityUser {
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String GENDER = "gender";
    public static final String USER_TYPE = "user_type";
    public static final String USER_STATUS = "user_status";
    public static final String NICK_NAME = "nick_name";

    @Id
    @Column("id")
    private Long id;

    @CreatedDate
    @Column("create_time")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column("update_time")
    private LocalDateTime updateTime;

    @Column("deleted")
    private Boolean deleted;

    @Column("user_name")
    private String userName;

    @Column("user_password")
    private String userPassword;

    @Column("nick_name")
    private String nickName;

    @Column("avatar")
    private String avatar;

    @Column("user_profile")
    private String userProfile;

    @Column("gender")
    private Integer gender;

    @Column("user_type")
    private String userType;

    @Column("user_status")
    private String userStatus;
}

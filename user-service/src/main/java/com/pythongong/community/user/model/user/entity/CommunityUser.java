package com.pythongong.community.user.model.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Table(name = "community_user")
@Entity
public class CommunityUser {
    public static final String USER_NAME = "user_name";
    public static final String USER_PASSWORD = "user_password";
    public static final String GENDER = "gender";
    public static final String USER_TYPE = "user_type";
    public static final String USER_STATUS = "user_status";
    public static final String NICK_NAME = "nick_name";

    @Id
    @Column(name = "id")
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "user_name", length = 20) // Added length = 20
    private String userName;

    @Column(name = "user_password", length = 100) // Added length = 100
    private String userPassword;

    @Column(name = "nick_name", length = 20) // Added length = 20 (based on SQL)
    private String nickName;

    @Column(name = "avatar", length = 255) // Added length = 255
    private String avatar;

    @Column(name = "user_profile", length = 100) // Added length = 100
    private String userProfile;

    @Column(name = "gender")
    private Short gender;

    @Column(name = "user_type", length = 20) // Added length = 20
    private String userType;

    @Column(name = "user_status", length = 20) // Added length = 20
    private String userStatus;
}

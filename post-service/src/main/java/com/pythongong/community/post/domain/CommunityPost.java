package com.pythongong.community.post.domain;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Table("community_post")
public class CommunityPost {
    @Id
    @Column("id")
    private Long id;

    @CreatedDate
    @Column("create_time")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column("update_time")
    private LocalDateTime updateTime;

    @Column("create_user_id")
    private Long createUserId;

    @Column("update_user_id")
    private Long updateUserId;

    @Column("content")
    private String content;

    @Column("post_status")
    private String postStatus;

    @Column("post_type")
    private String postType;
}
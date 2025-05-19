package com.pythongong.community.post.dto;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PostResponse {
    private Long id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUserId;
    private String authorName;
    private String content;
    private String postStatus;
    private String postType;
}
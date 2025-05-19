package com.pythongong.community.post.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record CreatePostRequest(
    @NotEmpty(message = "Content cannot be empty")
    @Size(min = 1, max = 1000, message = "Content must be between 1 and 1000 characters")
    String content,

    @NotEmpty(message = "Post type cannot be empty")
    @Size(max = 20, message = "Post type cannot exceed 20 characters")
    String postType
) {}
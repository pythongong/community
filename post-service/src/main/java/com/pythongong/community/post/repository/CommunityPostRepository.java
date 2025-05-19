package com.pythongong.community.post.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import com.pythongong.community.post.domain.CommunityPost;

public interface CommunityPostRepository extends ReactiveCrudRepository<CommunityPost, Long> {
}
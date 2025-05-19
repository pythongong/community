package com.pythongong.community.post.service;

import org.springframework.stereotype.Service;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;

import com.pythongong.community.infras.exception.CommunityException;
import com.pythongong.community.infras.web.AuthUserContext;
import com.pythongong.community.post.domain.CommunityPost;
import com.pythongong.community.post.repository.CommunityPostRepository;
import com.pythongong.community.post.dto.PostResponse;
import com.pythongong.community.post.dto.CreatePostRequest;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class PostService {
    private final CommunityPostRepository postRepository;
    private final R2dbcEntityTemplate template;
    private final UserServiceClient userServiceClient;

    private static final String BANNED_MESSAGE = "This post has been banned";
    private static final String NOT_FOUND_MESSAGE = "Post not found";

    public Mono<PostResponse> createPost(CreatePostRequest request) {
        Long userId = AuthUserContext.getCurrentUserId();
        CommunityPost post = new CommunityPost()
            .setCreateUserId(userId)
            .setUpdateUserId(userId)
            .setContent(request.content())
            .setPostType(request.postType())
            .setPostStatus("DRAFT");

        return postRepository.save(post)
            .flatMap(this::enrichPostWithAuthor);
    }

    public Mono<PostResponse> getPost(Long id) {
        return postRepository.findById(id)
            .switchIfEmpty(Mono.error(new CommunityException(NOT_FOUND_MESSAGE)))
            .flatMap(post -> {
                if ("BANNED".equals(post.getPostStatus())) {
                    post.setContent(BANNED_MESSAGE);
                }
                return enrichPostWithAuthor(post);
            });
    }

    public Flux<PostResponse> getAllPosts() {
        return postRepository.findAll()
            .flatMap(post -> {
                if ("BANNED".equals(post.getPostStatus())) {
                    post.setContent(BANNED_MESSAGE);
                }
                return enrichPostWithAuthor(post);
            });
    }

    public Mono<PostResponse> updatePost(Long id, CreatePostRequest request) {
        Long userId = AuthUserContext.getCurrentUserId();
        return postRepository.findById(id)
            .switchIfEmpty(Mono.error(new CommunityException(NOT_FOUND_MESSAGE)))
            .flatMap(post -> {
                if (!userId.equals(post.getCreateUserId())) {
                    return Mono.error(new CommunityException("Unauthorized to update this post"));
                }
                post.setContent(request.content())
                    .setPostType(request.postType())
                    .setUpdateUserId(userId);
                return postRepository.save(post);
            })
            .flatMap(this::enrichPostWithAuthor);
    }

    public Mono<Void> deletePost(Long id) {
        Long userId = AuthUserContext.getCurrentUserId();
        return postRepository.findById(id)
            .switchIfEmpty(Mono.error(new CommunityException(NOT_FOUND_MESSAGE)))
            .flatMap(post -> {
                if (!userId.equals(post.getCreateUserId())) {
                    return Mono.error(new CommunityException("Unauthorized to delete this post"));
                }
                return postRepository.deleteById(id);
            });
    }

    private Mono<PostResponse> enrichPostWithAuthor(CommunityPost post) {
        return userServiceClient.getUserName(post.getCreateUserId())
            .map(authorName -> {
                PostResponse response = new PostResponse()
                    .setId(post.getId())
                    .setCreateTime(post.getCreateTime())
                    .setUpdateTime(post.getUpdateTime())
                    .setCreateUserId(post.getCreateUserId())
                    .setAuthorName(authorName)
                    .setContent(post.getContent())
                    .setPostStatus(post.getPostStatus())
                    .setPostType(post.getPostType());
                return response;
            });
    }
}
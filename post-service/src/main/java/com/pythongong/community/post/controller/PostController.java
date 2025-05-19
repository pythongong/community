package com.pythongong.community.post.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import com.pythongong.community.post.service.PostService;
import com.pythongong.community.post.dto.PostResponse;
import com.pythongong.community.post.dto.CreatePostRequest;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/front/post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping("/auth/create")
    public Mono<PostResponse> createPost(@Validated @RequestBody CreatePostRequest request) {
        return postService.createPost(request);
    }

    @GetMapping("/{id}")
    public Mono<PostResponse> getPost(@PathVariable Long id) {
        return postService.getPost(id);
    }

    @GetMapping("/list")
    public Flux<PostResponse> getAllPosts() {
        return postService.getAllPosts();
    }

    @PutMapping("/auth/{id}")
    public Mono<PostResponse> updatePost(@PathVariable Long id, @Validated @RequestBody CreatePostRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/auth/{id}")
    public Mono<Void> deletePost(@PathVariable Long id) {
        return postService.deletePost(id);
    }
}
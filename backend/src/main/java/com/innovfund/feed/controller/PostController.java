package com.innovfund.feed.controller;

import com.innovfund.common.PageResponse;
import com.innovfund.feed.dto.*;
import com.innovfund.feed.service.PostService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/api/posts")
    public ResponseEntity<PostDto> create(@AuthenticationPrincipal SecurityUser principal,
                                           @Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.create(principal.getUser(), request));
    }

    @GetMapping("/api/posts/feed")
    public PageResponse<PostDto> feed(@AuthenticationPrincipal SecurityUser principal, Pageable pageable) {
        return postService.feed(principal == null ? null : principal.getUser(), pageable);
    }

    @GetMapping("/api/posts/startup/{startupId}")
    public List<PostDto> byStartup(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID startupId) {
        return postService.byStartup(principal == null ? null : principal.getUser(), startupId);
    }

    @GetMapping("/api/posts/trending")
    public List<PostDto> trending(@AuthenticationPrincipal SecurityUser principal,
                                   @RequestParam(defaultValue = "10") int limit) {
        return postService.trending(principal == null ? null : principal.getUser(), limit);
    }

    @DeleteMapping("/api/posts/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        postService.delete(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/posts/{id}/like")
    public ResponseEntity<Void> like(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        postService.like(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/posts/{id}/like")
    public ResponseEntity<Void> unlike(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        postService.unlike(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/posts/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@AuthenticationPrincipal SecurityUser principal,
                                                  @PathVariable UUID id,
                                                  @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.addComment(principal.getUser(), id, request));
    }

    @GetMapping("/api/posts/{id}/comments")
    public List<CommentDto> comments(@PathVariable UUID id) {
        return postService.comments(id);
    }

    @DeleteMapping("/api/comments/{id}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        postService.deleteComment(principal.getUser(), id);
        return ResponseEntity.noContent().build();
    }
}

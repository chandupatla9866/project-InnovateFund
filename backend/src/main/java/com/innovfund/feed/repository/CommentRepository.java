package com.innovfund.feed.repository;

import com.innovfund.feed.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findAllByPostIdOrderByCreatedAtAsc(UUID postId);
    long countByPostId(UUID postId);
}

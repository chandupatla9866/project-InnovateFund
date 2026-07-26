package com.innovfund.feed.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.PageResponse;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.feed.dto.*;
import com.innovfund.feed.entity.Comment;
import com.innovfund.feed.entity.Like;
import com.innovfund.feed.entity.Post;
import com.innovfund.feed.repository.CommentRepository;
import com.innovfund.feed.repository.LikeRepository;
import com.innovfund.feed.repository.PostRepository;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.startup.entity.Startup;
import com.innovfund.startup.service.StartupService;
import com.innovfund.user.entity.User;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final StartupService startupService;
    private final UserDisplayNameService userDisplayNameService;
    private final NotificationService notificationService;

    @Transactional
    public PostDto create(User author, CreatePostRequest request) {
        Startup startup = null;
        if (request.startupId() != null) {
            startup = startupService.findOrThrow(request.startupId());
        }
        Post post = Post.builder()
                .author(author)
                .startup(startup)
                .type(request.type())
                .text(request.text())
                .mediaUrl(request.mediaUrl())
                .build();
        return toDto(postRepository.save(post), author);
    }

    @Transactional(readOnly = true)
    public PageResponse<PostDto> feed(User viewer, Pageable pageable) {
        Page<Post> page = postRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PageResponse.from(page.map(p -> toDto(p, viewer)));
    }

    @Transactional(readOnly = true)
    public List<PostDto> byStartup(User viewer, UUID startupId) {
        return postRepository.findAllByStartupIdOrderByCreatedAtDesc(startupId).stream()
                .map(p -> toDto(p, viewer))
                .toList();
    }

    /**
     * Trending Score = likes×1 + comments×2 + recency bonus (linearly decaying over 7 days).
     * No AI involved — a straightforward, explainable ranking formula over recent posts.
     */
    @Transactional(readOnly = true)
    public List<PostDto> trending(User viewer, int limit) {
        Instant windowStart = Instant.now().minus(Duration.ofDays(7));
        List<Post> recent = postRepository.findAllByCreatedAtAfter(windowStart);

        return recent.stream()
                .sorted(Comparator.comparingDouble((Post p) -> trendingScore(p)).reversed())
                .limit(limit)
                .map(p -> toDto(p, viewer))
                .toList();
    }

    private double trendingScore(Post post) {
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        double hoursOld = Duration.between(post.getCreatedAt(), Instant.now()).toMinutes() / 60.0;
        double recencyBonus = Math.max(0, 48 - hoursOld);
        return likeCount * 1.0 + commentCount * 2.0 + recencyBonus;
    }

    @Transactional
    public void delete(User author, UUID postId) {
        Post post = findOrThrow(postId);
        if (!post.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedCustomException("You can only delete your own posts");
        }
        postRepository.delete(post);
    }

    @Transactional
    public void like(User user, UUID postId) {
        Post post = findOrThrow(postId);
        if (likeRepository.findByPostIdAndUserId(postId, user.getId()).isEmpty()) {
            likeRepository.save(Like.builder().post(post).user(user).build());
            if (!post.getAuthor().getId().equals(user.getId())) {
                String link = post.getStartup() != null
                        ? "/startups/" + post.getStartup().getId() + "#post-" + post.getId()
                        : "/feed";
                notificationService.notify(post.getAuthor(), NotificationType.NEW_POST_LIKE,
                        userDisplayNameService.resolveFullName(user) + " liked your post",
                        link);
            }
        }
    }

    @Transactional
    public void unlike(User user, UUID postId) {
        likeRepository.deleteByPostIdAndUserId(postId, user.getId());
    }

    @Transactional
    public CommentDto addComment(User author, UUID postId, CreateCommentRequest request) {
        Post post = findOrThrow(postId);
        Comment comment = Comment.builder().post(post).author(author).text(request.text()).build();
        comment = commentRepository.save(comment);
        if (!post.getAuthor().getId().equals(author.getId())) {
            String link = post.getStartup() != null
                    ? "/startups/" + post.getStartup().getId() + "#post-" + post.getId()
                    : "/feed";
            notificationService.notify(post.getAuthor(), NotificationType.NEW_COMMENT,
                    userDisplayNameService.resolveFullName(author) + " commented on your post",
                    link);
        }
        return new CommentDto(comment.getId(), postId, author.getId(),
                userDisplayNameService.resolveFullName(author), comment.getText(), comment.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<CommentDto> comments(UUID postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(c -> new CommentDto(c.getId(), postId, c.getAuthor().getId(),
                        userDisplayNameService.resolveFullName(c.getAuthor()), c.getText(), c.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void deleteComment(User author, UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        if (!comment.getAuthor().getId().equals(author.getId())) {
            throw new AccessDeniedCustomException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    private Post findOrThrow(UUID postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }

    private PostDto toDto(Post post, User viewer) {
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        boolean likedByMe = viewer != null && likeRepository.findByPostIdAndUserId(post.getId(), viewer.getId()).isPresent();
        Startup startup = post.getStartup();
        return new PostDto(
                post.getId(),
                post.getAuthor().getId(),
                userDisplayNameService.resolveFullName(post.getAuthor()),
                startup != null ? startup.getId() : null,
                startup != null ? startup.getName() : null,
                startup != null ? startup.getLogoUrl() : null,
                post.getType(),
                post.getText(),
                post.getMediaUrl(),
                likeCount,
                commentCount,
                likedByMe,
                post.getCreatedAt()
        );
    }
}

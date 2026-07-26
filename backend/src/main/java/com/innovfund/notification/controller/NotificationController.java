package com.innovfund.notification.controller;

import com.innovfund.common.PageResponse;
import com.innovfund.notification.dto.NotificationDto;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public PageResponse<NotificationDto> list(@AuthenticationPrincipal SecurityUser principal, Pageable pageable) {
        return notificationService.list(principal.getUser(), pageable);
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount(@AuthenticationPrincipal SecurityUser principal) {
        return Map.of("count", notificationService.unreadCount(principal.getUser()));
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID id) {
        notificationService.markAsRead(principal.getUser(), id);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(@AuthenticationPrincipal SecurityUser principal) {
        notificationService.markAllAsRead(principal.getUser());
    }
}

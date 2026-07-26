package com.innovfund.notification.service;

import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.PageResponse;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.email.service.EmailService;
import com.innovfund.notification.dto.NotificationDto;
import com.innovfund.notification.entity.Notification;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.repository.NotificationRepository;
import com.innovfund.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public void notify(User recipient, NotificationType type, String message, String link) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .message(message)
                .link(link)
                .build();
        notificationRepository.save(notification);

        String url = link != null ? frontendUrl + link : frontendUrl;
        emailService.send(recipient.getEmail(), "InnovateFund", message + "\n\n" + url);
    }

    @Transactional(readOnly = true)
    public PageResponse<NotificationDto> list(User user, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAllByRecipientIdOrderByCreatedAtDesc(user.getId(), pageable);
        return PageResponse.from(page.map(this::toDto));
    }

    public long unreadCount(User user) {
        return notificationRepository.countByRecipientIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(User user, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("Not your notification");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllAsRead(user.getId());
    }

    private NotificationDto toDto(Notification n) {
        return new NotificationDto(n.getId(), n.getType(), n.getMessage(), n.getLink(), n.isRead(), n.getCreatedAt());
    }
}

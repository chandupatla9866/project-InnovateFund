package com.innovfund.chat.service;

import com.innovfund.chat.dto.ChatMessageDto;
import com.innovfund.chat.dto.ConversationDto;
import com.innovfund.chat.entity.ChatMessage;
import com.innovfund.chat.repository.ChatMessageRepository;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.interest.service.StartupInterestService;
import com.innovfund.notification.entity.NotificationType;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import com.innovfund.user.service.UserDisplayNameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final UserDisplayNameService userDisplayNameService;
    private final StartupInterestService startupInterestService;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageDto sendMessage(User sender, UUID recipientId, String text) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));

        assertCanChat(sender, recipient);

        ChatMessage message = chatMessageRepository.save(
                ChatMessage.builder().sender(sender).recipient(recipient).text(text).build());

        ChatMessageDto dto = toDto(message);

        messagingTemplate.convertAndSendToUser(recipient.getEmail(), "/queue/messages", dto);

        notificationService.notify(recipient, NotificationType.NEW_MESSAGE,
                userDisplayNameService.resolveFullName(sender) + " sent you a message", "/chat/" + sender.getId());

        return dto;
    }

    @Transactional
    public List<ChatMessageDto> getConversation(User user, UUID otherUserId) {
        chatMessageRepository.markConversationRead(user.getId(), otherUserId);
        return chatMessageRepository.findConversation(user.getId(), otherUserId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(User user) {
        List<ChatMessage> all = chatMessageRepository.findAllForUser(user.getId());
        Map<UUID, List<ChatMessage>> byCounterpart = new LinkedHashMap<>();
        for (ChatMessage m : all) {
            UUID counterpartId = m.getSender().getId().equals(user.getId()) ? m.getRecipient().getId() : m.getSender().getId();
            byCounterpart.computeIfAbsent(counterpartId, k -> new java.util.ArrayList<>()).add(m);
        }

        return byCounterpart.entrySet().stream()
                .map(entry -> {
                    UUID counterpartId = entry.getKey();
                    List<ChatMessage> messages = entry.getValue();
                    ChatMessage last = messages.get(0);
                    User counterpart = last.getSender().getId().equals(counterpartId) ? last.getSender() : last.getRecipient();
                    long unread = chatMessageRepository.countBySenderIdAndRecipientIdAndReadFalse(counterpartId, user.getId());
                    return new ConversationDto(counterpartId, userDisplayNameService.resolveFullName(counterpart),
                            last.getText(), last.getCreatedAt(), unread);
                })
                .sorted(Comparator.comparing(ConversationDto::lastMessageAt).reversed())
                .toList();
    }

    /**
     * Founder-investor chat only opens once the founder has accepted that investor's expressed
     * interest in one of their startups — matches the product workflow (Express Interest -> Founder
     * Accepts -> Chat Opens). All other role pairings (admin, founder-founder, investor-investor)
     * are left unrestricted since the spec doesn't address them.
     */
    private void assertCanChat(User sender, User recipient) {
        User investor = sender.getRole() == Role.INVESTOR ? sender : recipient.getRole() == Role.INVESTOR ? recipient : null;
        User founder = sender.getRole() == Role.FOUNDER ? sender : recipient.getRole() == Role.FOUNDER ? recipient : null;
        if (investor == null || founder == null || investor.getId().equals(founder.getId())) {
            return;
        }
        if (!startupInterestService.hasAcceptedInterest(investor.getId(), founder.getId())) {
            throw new AccessDeniedCustomException(
                    "Chat opens once the founder accepts the investor's interest in one of their startups");
        }
    }

    private ChatMessageDto toDto(ChatMessage m) {
        return new ChatMessageDto(m.getId(), m.getSender().getId(), m.getRecipient().getId(), m.getText(), m.isRead(), m.getCreatedAt());
    }
}

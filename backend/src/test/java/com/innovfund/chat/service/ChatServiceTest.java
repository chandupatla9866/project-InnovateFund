package com.innovfund.chat.service;

import com.innovfund.chat.entity.ChatMessage;
import com.innovfund.chat.repository.ChatMessageRepository;
import com.innovfund.common.AccessDeniedCustomException;
import com.innovfund.interest.service.StartupInterestService;
import com.innovfund.notification.service.NotificationService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import com.innovfund.user.service.UserDisplayNameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Founder<->investor chat is gated behind an accepted StartupInterest (Express Interest -> Founder
 * Accepts -> Chat Opens). This is enforced server-side independent of any UI button hiding —
 * these tests confirm the gate actually blocks, and that it only applies to founder/investor pairs.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock private ChatMessageRepository chatMessageRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private UserDisplayNameService userDisplayNameService;
    @Mock private StartupInterestService startupInterestService;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @InjectMocks private ChatService chatService;

    private User user(Role role) {
        return User.builder().id(UUID.randomUUID()).email(role + "@test.com").role(role).build();
    }

    private void stubRecipientLookup(User recipient) {
        when(userRepository.findById(recipient.getId())).thenReturn(Optional.of(recipient));
    }

    private void stubSaveEcho() {
        when(chatMessageRepository.save(any())).thenAnswer(inv -> {
            ChatMessage m = inv.getArgument(0);
            if (m.getId() == null) m.setId(UUID.randomUUID());
            if (m.getCreatedAt() == null) m.setCreatedAt(java.time.Instant.now());
            return m;
        });
    }

    @Test
    void sendMessage_blocksFounderToInvestorWithoutAcceptedInterest() {
        User founder = user(Role.FOUNDER);
        User investor = user(Role.INVESTOR);
        stubRecipientLookup(investor);
        when(startupInterestService.hasAcceptedInterest(investor.getId(), founder.getId())).thenReturn(false);

        assertThatThrownBy(() -> chatService.sendMessage(founder, investor.getId(), "hi"))
                .isInstanceOf(AccessDeniedCustomException.class)
                .hasMessageContaining("accepts the investor's interest");
    }

    @Test
    void sendMessage_allowsFounderToInvestorOnceInterestAccepted() {
        User founder = user(Role.FOUNDER);
        User investor = user(Role.INVESTOR);
        stubRecipientLookup(investor);
        stubSaveEcho();
        when(startupInterestService.hasAcceptedInterest(investor.getId(), founder.getId())).thenReturn(true);
        lenient().when(userDisplayNameService.resolveFullName(any())).thenReturn("Someone");

        assertThatCode(() -> chatService.sendMessage(founder, investor.getId(), "hi")).doesNotThrowAnyException();
    }

    @Test
    void sendMessage_isUnrestrictedBetweenTwoFounders() {
        User founderA = user(Role.FOUNDER);
        User founderB = user(Role.FOUNDER);
        stubRecipientLookup(founderB);
        stubSaveEcho();
        lenient().when(userDisplayNameService.resolveFullName(any())).thenReturn("Someone");

        assertThatCode(() -> chatService.sendMessage(founderA, founderB.getId(), "hi")).doesNotThrowAnyException();
        // Role pairing never involves an investor, so the interest gate is never even consulted.
        org.mockito.Mockito.verifyNoInteractions(startupInterestService);
    }

    @Test
    void sendMessage_isUnrestrictedWhenAdminIsInvolved() {
        User admin = user(Role.ADMIN);
        User investor = user(Role.INVESTOR);
        stubRecipientLookup(investor);
        stubSaveEcho();
        lenient().when(userDisplayNameService.resolveFullName(any())).thenReturn("Someone");

        assertThatCode(() -> chatService.sendMessage(admin, investor.getId(), "hi")).doesNotThrowAnyException();
        org.mockito.Mockito.verifyNoInteractions(startupInterestService);
    }
}

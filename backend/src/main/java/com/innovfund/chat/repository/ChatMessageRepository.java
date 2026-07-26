package com.innovfund.chat.repository;

import com.innovfund.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("""
            select m from ChatMessage m
            where (m.sender.id = :userId and m.recipient.id = :otherId)
               or (m.sender.id = :otherId and m.recipient.id = :userId)
            order by m.createdAt asc
            """)
    List<ChatMessage> findConversation(@Param("userId") UUID userId, @Param("otherId") UUID otherId);

    @Query("""
            select m from ChatMessage m
            where m.sender.id = :userId or m.recipient.id = :userId
            order by m.createdAt desc
            """)
    List<ChatMessage> findAllForUser(@Param("userId") UUID userId);

    long countBySenderIdAndRecipientIdAndReadFalse(UUID senderId, UUID recipientId);

    long countByRecipientIdAndReadFalse(UUID recipientId);

    @Modifying
    @Query("update ChatMessage m set m.read = true where m.sender.id = :otherId and m.recipient.id = :userId and m.read = false")
    void markConversationRead(@Param("userId") UUID userId, @Param("otherId") UUID otherId);
}

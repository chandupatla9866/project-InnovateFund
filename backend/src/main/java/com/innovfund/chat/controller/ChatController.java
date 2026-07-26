package com.innovfund.chat.controller;

import com.innovfund.chat.dto.ChatMessageDto;
import com.innovfund.chat.dto.ConversationDto;
import com.innovfund.chat.dto.SendMessageRequest;
import com.innovfund.chat.service.ChatService;
import com.innovfund.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public List<ConversationDto> conversations(@AuthenticationPrincipal SecurityUser principal) {
        return chatService.getConversations(principal.getUser());
    }

    @GetMapping("/messages/{otherUserId}")
    public List<ChatMessageDto> messages(@AuthenticationPrincipal SecurityUser principal, @PathVariable UUID otherUserId) {
        return chatService.getConversation(principal.getUser(), otherUserId);
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageDto> send(@AuthenticationPrincipal SecurityUser principal,
                                                @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(chatService.sendMessage(principal.getUser(), request.recipientId(), request.text()));
    }
}

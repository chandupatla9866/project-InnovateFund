package com.innovfund.chat.config;

import com.innovfund.security.JwtService;
import com.innovfund.security.SecurityUser;
import com.innovfund.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Authenticates STOMP CONNECT frames using the JWT sent in the "Authorization" native header.
 * A raw WebSocket handshake can't carry custom headers from browser JS, so authentication has to
 * happen at the STOMP protocol level instead of the HTTP handshake level (which stays permitAll).
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtService.isTokenValid(token)) {
                    String email = jwtService.extractEmail(token);
                    userRepository.findByEmail(email).ifPresent(user -> {
                        SecurityUser securityUser = new SecurityUser(user);
                        accessor.setUser(new UsernamePasswordAuthenticationToken(
                                securityUser, null, securityUser.getAuthorities()));
                    });
                }
            }
        }
        return message;
    }
}

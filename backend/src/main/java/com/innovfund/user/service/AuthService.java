package com.innovfund.user.service;

import com.innovfund.common.BadRequestException;
import com.innovfund.common.ResourceNotFoundException;
import com.innovfund.email.service.EmailService;
import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.security.JwtService;
import com.innovfund.user.dto.AuthResponse;
import com.innovfund.user.dto.LoginRequest;
import com.innovfund.user.dto.RegisterRequest;
import com.innovfund.user.dto.UserSummaryDto;
import com.innovfund.user.entity.PasswordResetToken;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.PasswordResetTokenRepository;
import com.innovfund.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final FounderProfileRepository founderProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDisplayNameService userDisplayNameService;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role())
                .enabled(true)
                .build();
        user = userRepository.save(user);

        if (request.role() == Role.FOUNDER) {
            FounderProfile profile = FounderProfile.builder()
                    .user(user)
                    .fullName(request.fullName())
                    .build();
            founderProfileRepository.save(profile);
        } else {
            InvestorProfile profile = InvestorProfile.builder()
                    .user(user)
                    .fullName(request.fullName())
                    .build();
            investorProfileRepository.save(profile);
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, new UserSummaryDto(user.getId(), user.getEmail(), user.getRole(), request.fullName()));
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, new UserSummaryDto(user.getId(), user.getEmail(), user.getRole(), userDisplayNameService.resolveFullName(user)));
    }

    /** Always completes normally whether or not the email is registered — avoids leaking which emails have accounts. */
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(30, ChronoUnit.MINUTES))
                    .build();
            passwordResetTokenRepository.save(resetToken);

            String link = frontendUrl + "/reset-password?token=" + token;
            emailService.send(user.getEmail(), "Reset your InnovateFund password",
                    "Click the link below to reset your password. This link expires in 30 minutes.\n\n" + link
                            + "\n\nIf you didn't request this, you can safely ignore this email.");
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset link"));
        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Invalid or expired reset link");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }

    public UserSummaryDto getCurrentUser(User user) {
        return new UserSummaryDto(user.getId(), user.getEmail(), user.getRole(), userDisplayNameService.resolveFullName(user));
    }
}

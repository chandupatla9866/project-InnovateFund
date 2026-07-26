package com.innovfund.oauth;

import com.innovfund.founder.entity.FounderProfile;
import com.innovfund.founder.repository.FounderProfileRepository;
import com.innovfund.investor.entity.InvestorProfile;
import com.innovfund.investor.repository.InvestorProfileRepository;
import com.innovfund.security.JwtService;
import com.innovfund.user.entity.Role;
import com.innovfund.user.entity.User;
import com.innovfund.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final FounderProfileRepository founderProfileRepository;
    private final InvestorProfileRepository investorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            response.sendRedirect(UriComponentsBuilder.fromUriString(redirectUri)
                    .queryParam("error", "no_email_from_google").build().toUriString());
            return;
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> createUser(request, email, name));

        String token = jwtService.generateToken(user);
        String target = UriComponentsBuilder.fromUriString(redirectUri).queryParam("token", token).build().toUriString();
        response.sendRedirect(target);
    }

    private User createUser(HttpServletRequest request, String email, String name) {
        Object stashedRole = request.getSession().getAttribute(OAuth2StartController.SESSION_ROLE_ATTRIBUTE);
        Role role = stashedRole != null ? Role.valueOf(stashedRole.toString()) : Role.FOUNDER;

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .role(role)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        String fullName = (name == null || name.isBlank()) ? email : name;
        if (role == Role.FOUNDER) {
            founderProfileRepository.save(FounderProfile.builder().user(user).fullName(fullName).build());
        } else {
            investorProfileRepository.save(InvestorProfile.builder().user(user).fullName(fullName).build());
        }
        return user;
    }
}

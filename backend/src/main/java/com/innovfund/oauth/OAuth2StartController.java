package com.innovfund.oauth;

import com.innovfund.common.BadRequestException;
import com.innovfund.user.entity.Role;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Stashes the desired role (only used if this Google login results in a brand-new account —
 * an existing account keeps its existing role) in the session, then kicks off the standard
 * Spring Security OAuth2 authorization redirect. A real browser navigation, not an XHR call.
 */
@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2StartController {

    public static final String SESSION_ROLE_ATTRIBUTE = "oauth2_signup_role";

    @Value("${GOOGLE_CLIENT_ID:}")
    private String googleClientId;

    @GetMapping("/enabled")
    public Map<String, Boolean> enabled() {
        return Map.of("enabled", !googleClientId.isBlank());
    }

    @GetMapping("/start")
    public void start(@RequestParam(defaultValue = "FOUNDER") String role,
                       HttpSession session,
                       HttpServletResponse response) throws IOException {
        Role parsedRole;
        try {
            parsedRole = Role.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid role: " + role);
        }
        if (parsedRole == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be self-registered");
        }
        session.setAttribute(SESSION_ROLE_ATTRIBUTE, parsedRole.name());
        response.sendRedirect("/oauth2/authorization/google");
    }
}

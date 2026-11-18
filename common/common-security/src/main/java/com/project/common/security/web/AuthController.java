package com.project.common.security.web;

import com.project.common.security.authentication.AuthenticationService;
import com.project.common.security.authentication.TokenPair;
import com.project.common.security.web.dto.LoginRequest;
import com.project.common.security.web.dto.RefreshTokenRequest;
import com.project.common.security.web.dto.TokenResponse;
import com.project.common.web.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication endpoints.
 *
 * <p>Deliberately unconditional. The controller used to be annotated with
 * {@code @ConditionalOnBean(AuthenticationService.class)}, which depends on configuration
 * class scan order: the security package is scanned before the module that publishes the
 * credential port, so the endpoints silently disappeared and every login returned 404.
 * Now a missing {@code AuthenticationUserPort} fails the startup with a clear message
 * instead of producing an application that answers but cannot authenticate.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final int MAX_USER_AGENT_LENGTH = 300;

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletRequest servletRequest) {
        TokenPair tokens = authenticationService.login(
                request.username(),
                request.password(),
                clientIp(servletRequest),
                truncate(servletRequest.getHeader(HttpHeaders.USER_AGENT)));
        return ApiResponse.of(TokenResponse.from(tokens));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.of(TokenResponse.from(authenticationService.refresh(request.refreshToken())));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.refreshToken() != null) {
            authenticationService.logout(request.refreshToken());
        }
        return ApiResponse.of(null, "Logged out");
    }

    private static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_USER_AGENT_LENGTH ? value : value.substring(0, MAX_USER_AGENT_LENGTH);
    }
}

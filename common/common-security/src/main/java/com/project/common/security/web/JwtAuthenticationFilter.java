package com.project.common.security.web;

import com.project.common.security.jwt.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Reads the bearer token, validates it and populates the security context.
 *
 * <p>An invalid or expired token never produces an error here: the request simply stays
 * anonymous and the entry point answers 401 if the endpoint requires authentication. That
 * keeps public endpoints reachable even when a stale token is attached.</p>
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticate(request, header.substring(BEARER_PREFIX.length()).trim());
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            var jwt = jwtTokenService.decode(token);
            Long userId = Long.valueOf(jwt.getSubject());
            String username = jwt.getClaimAsString("username");
            Set<String> roles = toSet(jwt.getClaimAsStringList("roles"));
            Set<String> permissions = toSet(jwt.getClaimAsStringList("perms"));

            List<GrantedAuthority> authorities = new ArrayList<>();
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
            permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

            var principal = new AuthenticatedPrincipal(userId, username, roles);
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException ex) {
            // Signature, expiry or claim problems: treat the request as anonymous.
            SecurityContextHolder.clearContext();
            log.debug("Rejected bearer token: {}", ex.getMessage());
        }
    }

    private static Set<String> toSet(List<String> values) {
        return values == null ? Set.of() : Set.copyOf(values);
    }
}

package com.project.common.security.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

/**
 * Answers unauthenticated requests with the platform error envelope instead of the
 * default HTML/empty body.
 *
 * <p>It delegates to {@link HandlerExceptionResolver}, so the response is produced by
 * {@code GlobalExceptionHandler} — one error shape, one place.</p>
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public RestAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {
        resolver.resolveException(request, response, null,
                new com.project.common.core.error.AuthenticationException());
    }
}

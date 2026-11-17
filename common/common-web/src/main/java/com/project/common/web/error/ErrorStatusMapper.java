package com.project.common.web.error;

import com.project.common.core.error.AuthenticationException;
import com.project.common.core.error.AuthorizationException;
import com.project.common.core.error.BusinessException;
import com.project.common.core.error.DuplicateResourceException;
import com.project.common.core.error.RateLimitExceededException;
import com.project.common.core.error.ResourceNotFoundException;
import com.project.common.core.error.ValidationException;
import org.springframework.http.HttpStatus;

/**
 * Single place that decides which HTTP status a failure maps to.
 *
 * <p>Keeping this out of the exceptions themselves means {@code common-core} stays free
 * of Spring types, while the mapping is still one table, not scattered conditionals.</p>
 */
public final class ErrorStatusMapper {

    private ErrorStatusMapper() {
    }

    public static HttpStatus statusOf(BusinessException exception) {
        if (exception instanceof ResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        }
        if (exception instanceof DuplicateResourceException) {
            return HttpStatus.CONFLICT;
        }
        if (exception instanceof AuthenticationException) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (exception instanceof AuthorizationException) {
            return HttpStatus.FORBIDDEN;
        }
        if (exception instanceof RateLimitExceededException) {
            return HttpStatus.TOO_MANY_REQUESTS;
        }
        if (exception instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.BAD_REQUEST;
    }
}

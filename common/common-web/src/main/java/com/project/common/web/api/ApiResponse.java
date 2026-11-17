package com.project.common.web.api;

import com.project.common.core.error.ErrorItem;
import com.project.common.web.error.CorrelationIds;

import java.time.Instant;
import java.util.List;

/**
 * Single response shape of the public API.
 *
 * <p>Every endpoint returns this envelope, success or failure, so clients have one
 * parsing path. {@code traceId} is always present and matches the correlation id in the
 * logs and in {@code SYSTEM_ERROR_LOG}; it is the only debugging hint a client gets.</p>
 *
 * @param <T> payload type
 */
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        List<ErrorItem> errors,
        Instant timestamp,
        String traceId) {

    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(true, "OK", data, List.of(), Instant.now(), CorrelationIds.currentOrCreate());
    }

    public static <T> ApiResponse<T> of(T data, String message) {
        return new ApiResponse<>(true, message, data, List.of(), Instant.now(), CorrelationIds.currentOrCreate());
    }

    public static <T> ApiResponse<T> failure(String message, List<ErrorItem> errors) {
        return new ApiResponse<>(false, message, null,
                errors == null ? List.of() : List.copyOf(errors),
                Instant.now(), CorrelationIds.currentOrCreate());
    }
}

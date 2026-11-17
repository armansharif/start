package com.project.common.web.error;

import com.project.common.core.error.BusinessException;
import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.error.ErrorItem;
import com.project.common.core.error.RateLimitExceededException;
import com.project.common.core.error.SystemException;
import com.project.common.core.error.ValidationException;
import com.project.common.web.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Central translation of failures into the API envelope.
 *
 * <p>Rules:</p>
 * <ul>
 *   <li>business failures carry their stable code and a localized message, no stack trace;</li>
 *   <li>unexpected failures are recorded through {@link SystemErrorRecorder} and the client
 *       only receives the generic code plus the correlation id;</li>
 *   <li>recording an error must never fail the request twice.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageResolver messages;
    private final ObjectProvider<SystemErrorRecorder> systemErrorRecorder;

    public GlobalExceptionHandler(MessageResolver messages,
                                  ObjectProvider<SystemErrorRecorder> systemErrorRecorder) {
        this.messages = messages;
        this.systemErrorRecorder = systemErrorRecorder;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException exception) {
        List<ErrorItem> errors = exception.getErrors().isEmpty()
                ? List.of(ErrorItem.of(exception.getErrorCode(),
                messages.resolve(exception.getErrorCode(), exception.getMessageArgs())))
                : exception.getErrors();
        return build(HttpStatus.BAD_REQUEST, messageOf(exception), errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBeanValidation(MethodArgumentNotValidException exception) {
        List<ErrorItem> errors = new ArrayList<>();
        exception.getBindingResult().getFieldErrors().forEach(fieldError ->
                errors.add(new ErrorItem(CommonErrorCode.VALIDATION_FAILED.code(),
                        fieldError.getField(), fieldError.getDefaultMessage())));
        exception.getBindingResult().getGlobalErrors().forEach(globalError ->
                errors.add(new ErrorItem(CommonErrorCode.VALIDATION_FAILED.code(),
                        null, globalError.getDefaultMessage())));
        return build(HttpStatus.BAD_REQUEST,
                messages.resolve(CommonErrorCode.VALIDATION_FAILED), errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
        List<ErrorItem> errors = new ArrayList<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            errors.add(new ErrorItem(CommonErrorCode.VALIDATION_FAILED.code(),
                    violation.getPropertyPath().toString(), violation.getMessage()));
        }
        return build(HttpStatus.BAD_REQUEST,
                messages.resolve(CommonErrorCode.VALIDATION_FAILED), errors);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<ApiResponse<Void>> handleMalformedRequest(Exception exception) {
        List<ErrorItem> errors = List.of(ErrorItem.of(CommonErrorCode.INVALID_ARGUMENT,
                messages.resolve(CommonErrorCode.INVALID_ARGUMENT)));
        log.debug("Rejected malformed request: {}", exception.getMessage());
        return build(HttpStatus.BAD_REQUEST, messages.resolve(CommonErrorCode.INVALID_ARGUMENT), errors);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException exception) {
        HttpStatus status = ErrorStatusMapper.statusOf(exception);
        String message = messageOf(exception);
        List<ErrorItem> errors = List.of(ErrorItem.of(exception.getErrorCode(), message));

        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status);
        if (exception instanceof RateLimitExceededException rateLimited) {
            builder.header(HttpHeaders.RETRY_AFTER, String.valueOf(rateLimited.getRetryAfterSeconds()));
        }
        return builder.body(ApiResponse.failure(message, errors));
    }

    @ExceptionHandler(SystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleSystem(SystemException exception,
                                                          HttpServletRequest request) {
        return recordAndFail(exception, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
        if (exception instanceof BusinessException businessException) {
            // Security and validation exceptions can arrive wrapped; keep the contract.
            return handleBusiness(businessException);
        }
        return recordAndFail(exception, request);
    }

    private ResponseEntity<ApiResponse<Void>> recordAndFail(Exception exception, HttpServletRequest request) {
        String correlationId = CorrelationIds.currentOrCreate();
        log.error("Unhandled system error [correlationId={}]", correlationId, exception);

        SystemErrorRecorder recorder = systemErrorRecorder.getIfAvailable();
        if (recorder != null) {
            try {
                recorder.record(toRecord(exception, request, correlationId));
            } catch (RuntimeException recordingFailure) {
                // Never let error logging replace the original failure.
                log.error("Could not persist system error [correlationId={}]", correlationId, recordingFailure);
            }
        }

        List<ErrorItem> errors = List.of(ErrorItem.of(CommonErrorCode.SYSTEM_ERROR,
                messages.resolve(CommonErrorCode.SYSTEM_ERROR)));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure(messages.resolve(CommonErrorCode.SYSTEM_ERROR), errors));
    }

    private SystemErrorRecord toRecord(Exception exception, HttpServletRequest request, String correlationId) {
        return new SystemErrorRecord(
                CommonErrorCode.SYSTEM_ERROR.code(),
                exception.getClass().getName(),
                exception.getMessage(),
                stackTraceOf(exception),
                request == null ? null : request.getRequestURI(),
                request == null ? null : request.getMethod(),
                null,
                correlationId,
                Instant.now());
    }

    private static String stackTraceOf(Throwable throwable) {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private String messageOf(BusinessException exception) {
        return messages.resolve(exception.getErrorCode(), exception.getMessageArgs());
    }

    private static ResponseEntity<ApiResponse<Void>> build(HttpStatusCode status,
                                                           String message,
                                                           List<ErrorItem> errors) {
        return ResponseEntity.status(status).body(ApiResponse.failure(message, errors));
    }
}

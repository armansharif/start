package com.project.common.web.error;

import com.project.common.core.error.CommonErrorCode;
import com.project.common.core.error.ResourceNotFoundException;
import com.project.common.core.error.ValidationException;
import com.project.common.web.api.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class GlobalExceptionHandlerTest {

    private final List<SystemErrorRecord> recorded = new ArrayList<>();
    private MockMvc mockMvc;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        SystemErrorRecorder recorder = recorded::add;
        ObjectProvider<SystemErrorRecorder> recorderProvider = mock(ObjectProvider.class);
        when(recorderProvider.getIfAvailable()).thenReturn(recorder);

        GlobalExceptionHandler handler = new GlobalExceptionHandler(
                new MessageResolver(new StaticMessageSource()), recorderProvider);

        mockMvc = MockMvcBuilders.standaloneSetup(new SampleController())
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void mapsNotFoundBusinessError() throws Exception {
        var response = mockMvc.perform(get("/not-found")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getContentAsString())
                .contains("\"success\":false")
                .contains("COMMON-1004")
                .contains("\"traceId\"");
        assertThat(recorded).isEmpty();
    }

    @Test
    void mapsValidationErrorsWithFieldDetails() throws Exception {
        var response = mockMvc.perform(get("/validation")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString())
                .contains("COMMON-1002")
                .contains("mobile");
        assertThat(recorded).isEmpty();
    }

    @Test
    void recordsUnexpectedErrorsAndHidesDetails() throws Exception {
        var response = mockMvc.perform(get("/unexpected")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString())
                .contains("COMMON-5000")
                .doesNotContain("boom")
                .doesNotContain("IllegalStateException")
                .doesNotContain("at com.project");
        assertThat(recorded).hasSize(1);
        assertThat(recorded.getFirst().exceptionClass()).isEqualTo(IllegalStateException.class.getName());
        assertThat(recorded.getFirst().correlationId()).isNotBlank();
        assertThat(recorded.getFirst().requestUri()).isEqualTo("/unexpected");
    }

    @Test
    void returnsSuccessEnvelopeUnchanged() throws Exception {
        var response = mockMvc.perform(get("/ok")).andReturn().getResponse();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString())
                .contains("\"success\":true")
                .contains("\"data\":\"payload\"");
    }

    @RestController
    static class SampleController {

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("User", 5L);
        }

        @GetMapping("/validation")
        void validation() {
            throw new ValidationException("mobile", CommonErrorCode.VALIDATION_FAILED);
        }

        @GetMapping("/unexpected")
        void unexpected() {
            throw new IllegalStateException("boom: internal detail that must not leak");
        }

        @GetMapping("/ok")
        ApiResponse<String> ok() {
            return ApiResponse.of("payload");
        }
    }
}

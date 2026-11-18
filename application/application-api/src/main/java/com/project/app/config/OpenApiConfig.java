package com.project.app.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description of the public API.
 *
 * <p>Without a declared security scheme Swagger UI shows no Authorize button, so a caller
 * can log in but cannot use the token on the secured endpoints. The scheme below is the
 * only thing needed: the filter already accepts {@code Authorization: Bearer <token>}.</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI backendPlatformOpenApi() {
        SecurityScheme bearer = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("""
                        Access token from POST /api/v1/auth/login.
                        Paste only the token value; Swagger adds the "Bearer " prefix itself.""");

        return new OpenAPI()
                .info(new Info()
                        .title("Backend Platform API")
                        .version("1.0.0")
                        .description("""
                                All endpoints live under /api/v1 and share one response envelope:
                                success, message, data, errors, timestamp, traceId.
                                1) POST /api/v1/auth/login  2) Authorize with accessToken  3) call the rest."""))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, bearer))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}

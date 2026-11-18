package com.project.common.security.config;

import com.project.common.core.security.CurrentUserProvider;
import com.project.common.security.authentication.AuthenticationService;
import com.project.common.security.authentication.AuthenticationUserPort;
import com.project.common.security.bruteforce.BruteForceGuard;
import com.project.common.security.bruteforce.InMemoryBruteForceGuard;
import com.project.common.security.jwt.JwtTokenService;
import com.project.common.security.token.JpaRefreshTokenStore;
import com.project.common.security.token.RefreshTokenRepository;
import com.project.common.security.token.RefreshTokenService;
import com.project.common.security.token.RefreshTokenStore;
import com.project.common.security.web.JwtAuthenticationFilter;
import com.project.common.security.web.RestAccessDeniedHandler;
import com.project.common.security.web.RestAuthenticationEntryPoint;
import com.project.common.security.web.SecurityContextCurrentUserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;

/**
 * Security wiring.
 *
 * <p>The filter chain is stateless: authentication travels in the {@code Authorization}
 * header, CSRF is therefore disabled (there is no cookie to forge), and every endpoint
 * except authentication and operational probes requires a valid token.</p>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfig {

    private static final int MIN_SECRET_BYTES = 32;

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Delegating encoder keeps existing hashes usable when the algorithm is upgraded.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(SecurityProperties properties) {
        String secret = properties.getJwt().getSecret();
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "app.security.jwt.secret must be configured with at least " + MIN_SECRET_BYTES
                            + " bytes (set the APP_JWT_SECRET environment variable)");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, SecurityProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getJwt().getIssuer()));
        return decoder;
    }

    @Bean
    public JwtTokenService jwtTokenService(JwtEncoder encoder, JwtDecoder decoder,
                                           SecurityProperties properties, Clock clock) {
        return new JwtTokenService(encoder, decoder, properties, clock);
    }

    @Bean
    @ConditionalOnMissingBean(RefreshTokenStore.class)
    public RefreshTokenStore refreshTokenStore(RefreshTokenRepository repository) {
        return new JpaRefreshTokenStore(repository);
    }

    @Bean
    public RefreshTokenService refreshTokenService(RefreshTokenStore store,
                                                   SecurityProperties properties,
                                                   Clock clock) {
        return new RefreshTokenService(store, properties, clock);
    }

    @Bean
    @ConditionalOnMissingBean(BruteForceGuard.class)
    public BruteForceGuard bruteForceGuard(SecurityProperties properties, Clock clock) {
        return new InMemoryBruteForceGuard(properties, clock);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService);
    }

    @Bean
    public CurrentUserProvider currentUserProvider() {
        return new SecurityContextCurrentUserProvider();
    }

    /**
     * Authentication requires a credential source. It is required rather than conditional:
     * a conditional bean here is evaluated in configuration scan order, which made the
     * login endpoint disappear depending on which package was scanned first.
     */
    @Bean
    public AuthenticationService authenticationService(AuthenticationUserPort users,
                                                       PasswordEncoder passwordEncoder,
                                                       JwtTokenService jwtTokenService,
                                                       RefreshTokenService refreshTokenService,
                                                       BruteForceGuard bruteForceGuard) {
        return new AuthenticationService(users, passwordEncoder, jwtTokenService,
                refreshTokenService, bruteForceGuard);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(properties.getCors().getAllowedOrigins());
        configuration.setAllowedMethods(properties.getCors().getAllowedMethods());
        configuration.setAllowedHeaders(properties.getCors().getAllowedHeaders());
        configuration.setAllowCredentials(properties.getCors().isAllowCredentials());
        configuration.setMaxAge(properties.getCors().getMaxAge());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtAuthenticationFilter,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler,
                                                   CorsConfigurationSource corsConfigurationSource)
            throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/logout").permitAll()
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // The static pages are shells only: the data they show still needs a token.
                        .requestMatchers("/", "/index.html", "/users.html", "/app.css").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

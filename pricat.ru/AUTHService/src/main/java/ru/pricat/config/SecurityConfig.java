package ru.pricat.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.pricat.util.AppConstants;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.secret-key}")
    private String jwtSecret;

    private final TokenBlacklistFilter tokenBlacklistFilter;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .addFilterBefore(tokenBlacklistFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                AppConstants.API_V1_AUTH_PATH + "/register",
                                AppConstants.API_V1_AUTH_PATH + "/login",
                                AppConstants.API_V1_AUTH_PATH + "/logout",
                                "/actuator/health",
                                "/actuator/info",
                                "/actuator/metrics"
                        ).permitAll()
                        .pathMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()
                        .pathMatchers(AppConstants.API_V1_AUTH_PATH + "/admin/**").hasRole("ADMIN")
                        .pathMatchers(AppConstants.API_V1_AUTH_PATH + "/**").authenticated()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverter()))
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        SecretKey secretKey = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }
}

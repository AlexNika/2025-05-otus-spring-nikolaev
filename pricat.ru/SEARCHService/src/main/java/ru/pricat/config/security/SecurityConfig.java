package ru.pricat.config.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import static ru.pricat.util.AppConstants.API_V1_SEARCH_PATH;

/**
 * Конфигурационный класс для настройки безопасности веб-приложения search-service.
 * Определяет правила доступа к ресурсам, фильтры и обработку ошибок аутентификации.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * Создает цепочку фильтров безопасности для веб-приложения.
     * Настраивает разрешения для различных URL-путей, OAuth2 с JWT, отключает CSRF,
     * форму логина и HTTP Basic, а также включает CORS.
     *
     * @param http объект для настройки HTTP-безопасности
     * @return настроенная цепочка фильтров безопасности
     * @throws Exception при ошибках конфигурации
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configurationAuthorizationRules(http);
        configurationOauth2ResourceServer(http);
        configurationSessionManagement(http);
        configurationCSRF(http);
        configurationFormLogin(http);
        configurationHttpBasic(http);
        configurationExceptionHandling(http);
        configurationAddFilterBefore(http);
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        return converter;
    }

    private void configurationAuthorizationRules(@NonNull HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(getPublicUrls()).permitAll()
                .requestMatchers(getWebUIAuthenticatedUrls()).authenticated()
                .requestMatchers(getAPIAuthenticatedUrls()).authenticated()
                .anyRequest().authenticated());
    }

    /**
     * Конфигурирует OAuth2 Resource Server для работы с JWT токенами.
     * Устанавливает конвертер для извлечения authorities из JWT.
     *
     * @param http объект HttpSecurity для настройки
     * @throws Exception при ошибках конфигурации
     */
    private void configurationOauth2ResourceServer(@NonNull HttpSecurity http) throws Exception {
        http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );
    }

    private void configurationSessionManagement(@NonNull HttpSecurity http) throws Exception {
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
    }

    private void configurationCSRF(@NonNull HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers(
                        "/api/**",
                        "/actuator/**"
                )
        );
    }

    private void configurationFormLogin(@NonNull HttpSecurity http) throws Exception {
        http.formLogin(AbstractHttpConfigurer::disable);
    }

    private void configurationHttpBasic(@NonNull HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable);
    }

    /**
     * Конфигурирует обработку исключений безопасности.
     * Устанавливает кастомную точку входа для аутентификации и обработчик access denied.
     *
     * @param http объект HttpSecurity для настройки
     * @throws Exception при ошибках конфигурации
     */
    private void configurationExceptionHandling(@NonNull HttpSecurity http) throws Exception {
        http.exceptionHandling(exception -> exception
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler((request, response, _) -> {
                    if (isApiRequest(request)) {
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\": \"Access Denied\"}");
                    } else {
                        log.warn("Access denied for user: {} to path: {}",
                                SecurityContextHolder.getContext().getAuthentication().getName(),
                                request.getRequestURI());
                        response.sendRedirect("/error?status=403&error=Access+Denied&message=You+don't+have+permission+to+access+this+page");
                    }
                })
        );
    }

    private void configurationAddFilterBefore(@NonNull HttpSecurity http) {
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private String[] getPublicUrls() {
        return new String[]{
                "/",
                "/webjars/**",
                "/js/**", "/css/**", "/images/**", "/favicon.ico",
                "/actuator/**"
        };
    }

    private String[] getWebUIAuthenticatedUrls() {
        return new String[]{
                "/search"
        };
    }

    private String[] getAPIAuthenticatedUrls() {
        return new String[]{
                API_V1_SEARCH_PATH + "/**"
        };
    }

    /**
     * Определяет, является ли запрос API запросом.
     * Используется для дифференцированной обработки ошибок.
     *
     * @param request HTTP запрос
     * @return true если запрос начинается с /api, иначе false
     */
    private boolean isApiRequest(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api");
    }
}

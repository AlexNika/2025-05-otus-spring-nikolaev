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
import ru.pricat.config.properties.InternalApiKeyConfig;

import static ru.pricat.util.AppConstants.API_V1_CLIENT_PATH;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final InternalApiKeyConfig internalApiKeyConfig;

    private final TokenRefreshFilter tokenRefreshFilter;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    /**
     * Конфигурирует цепочку фильтров безопасности Spring Security.
     * Определяет правила авторизации, обработку исключений и настройки CSRF.
     *
     * @param http объект для настройки безопасности
     * @return сконфигурированная цепочка фильтров безопасности
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

    /**
     * Фильтр для проверки X-API-Key при использовании внутренних endpoints
     * @return экземпляр InternalApiKeyFilter
     */
    @Bean
    public InternalApiKeyFilter internalApiKeyFilter() {
        String internalApiKey = internalApiKeyConfig.getInternalApiKey();
        log.debug("Internal API Key: {}", internalApiKey);
        return new InternalApiKeyFilter(internalApiKey);
    }

    private void configurationAuthorizationRules(@NonNull HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(getPublicUrls()).permitAll()
                .requestMatchers(getWebUIAuthenticatedUrls()).authenticated()
                .requestMatchers(getAPIAuthenticatedUrls()).authenticated()
                .requestMatchers(getAPIAdminUrls()).hasRole("ADMIN")
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
                        "/internal/**",
                        "/actuator/**",
                        "/v3/api-docs/**"
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
//                        response.sendRedirect("/login?error=access_denied");
                        response.sendRedirect("/error?status=403&error=Access+Denied&message=You+don't+have+permission+to+access+this+page");
                    }
                })
        );
    }

    private void configurationAddFilterBefore(@NonNull HttpSecurity http) {
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(internalApiKeyFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(tokenRefreshFilter, UsernamePasswordAuthenticationFilter.class);
    }

    private String[] getPublicUrls() {
        return new String[]{
                "/", "/login", "/register",
                "/internal/**",
                "/js/**", "/css/**", "/images/**", "/favicon.ico",
                "/actuator/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**",
                "/swagger-resources", "/configuration/ui", "/configuration/security", "/webjars/**",
                "/swagger-ui/index.html", "/swagger-ui/favicon.ico"
        };
    }

    private String[] getWebUIAuthenticatedUrls() {
        return new String[]{
                "/profile",
                "/profile-edit",
                "/clients",
                "/files"
        };
    }

    private String[] getAPIAuthenticatedUrls() {
        return new String[]{
                API_V1_CLIENT_PATH + "/**"
        };
    }

    private String[] getAPIAdminUrls() {
        return new String[]{
                API_V1_CLIENT_PATH + "/{username}"
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

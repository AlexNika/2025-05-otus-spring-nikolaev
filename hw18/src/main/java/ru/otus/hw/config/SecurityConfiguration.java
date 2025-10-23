package ru.otus.hw.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        configureCsrfAndHeaders(http);
        configureSessionManagement(http);
        configureAuthorizationRules(http);
        configureExceptionHandling(http);
        configureFormLoginAndLogout(http);
        configureUserDetailsService(http);

        return http.build();
    }

    private void configureCsrfAndHeaders(HttpSecurity http) throws Exception {
        http.csrf(csrf ->
                        csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers ->
                        headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
    }

    private void configureSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));
    }

    private void configureAuthorizationRules(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(getPublicUrls()).permitAll()
                .requestMatchers(getReaderViewUrls()).hasAnyRole("READER", "AUTHOR", "LIBRARIAN", "ADMIN")
                .requestMatchers(getCreatorUrls()).hasAnyRole("AUTHOR", "LIBRARIAN", "ADMIN")
                .requestMatchers(getCommentCreatorUrls()).hasAnyRole("READER", "AUTHOR", "LIBRARIAN", "ADMIN")
                .requestMatchers(getEditorUrls()).hasAnyRole("AUTHOR", "LIBRARIAN", "ADMIN")
                .requestMatchers(getDeleterUrls()).hasRole("ADMIN")
                .anyRequest().authenticated());
    }

    private void configureExceptionHandling(HttpSecurity http) throws Exception {
        http.exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response,
                                      accessDeniedException) ->
                        response.sendRedirect(request.getContextPath() + "/access-denied")));
    }

    private void configureFormLoginAndLogout(HttpSecurity http) throws Exception {
        http.formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
    }

    private void configureUserDetailsService(HttpSecurity http) throws Exception {
        http.userDetailsService(userDetailsService);
    }

    private String[] getPublicUrls() {
        return new String[]{
                "/",
                "/login",
                "/favicon.ico",
                "/actuator/**",
                "/h2-console/**"
        };
    }

    private String[] getReaderViewUrls() {
        return new String[]{
                "/authors",
                "/authors/{id}/details",
                "/genres",
                "/genres/{id}/details",
                "/books",
                "/books/{id}/details",
                "/books/{id}/comments"
        };
    }

    private String[] getCreatorUrls() {
        return new String[]{
                "/authors/add",
                "/genres/add",
                "/books/add"
        };
    }

    private String[] getCommentCreatorUrls() {
        return new String[]{
                "/books/*/comments/add"
        };
    }

    private String[] getEditorUrls() {
        return new String[]{
                "/authors/*/edit",
                "/genres/*/edit",
                "/books/*/edit",
                "/books/*/comments/*/edit"
        };
    }

    private String[] getDeleterUrls() {
        return new String[]{
                "/authors/*/delete",
                "/genres/*/delete",
                "/books/*/delete",
                "/books/*/comments/*/delete"
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
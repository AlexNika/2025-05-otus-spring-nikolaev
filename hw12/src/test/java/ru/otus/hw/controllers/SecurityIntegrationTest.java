package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Контроллер аутентификации ")
@Transactional
class SecurityIntegrationTest {

    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "admin";

    @Autowired
    private MockMvc mockMvc;

    @DisplayName("должен отображать форму логина без аутентификации")
    @Test
    void whenAccessLoginPageWithoutAuth_thenReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(view().name("login"))
                .andExpect(unauthenticated());
    }

    @ParameterizedTest
    @MethodSource("protectedPageUrls")
    @DisplayName("должен перенаправлять на логин при попытке доступа к защищенным страницам без аутентификации")
    void whenAccessProtectedPagesWithoutAuth_thenRedirectsToLogin(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());
    }

    @ParameterizedTest
    @MethodSource("protectedPageUrlsForAccess")
    @DisplayName("должен отображать защищенные страницы после успешной аутентификации")
    @WithMockUser(username = "admin")
    void whenAccessProtectedPagesWithAuth_thenReturnsPageView(String url) throws Exception {
        mockMvc.perform(get(url)
                        .header("Referer", "/"))
                .andExpect(authenticated());
    }

    @DisplayName("должен успешно аутентифицировать пользователя с правильными учетными данными")
    @Test
    void whenLoginWithCorrectCredentials_thenAuthSuccess() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user(TEST_USERNAME)
                        .password(TEST_PASSWORD))
                .andExpect(redirectedUrl("/"))
                .andExpect(authenticated());
    }

    @DisplayName("должен отображать ошибку при попытке аутентификации с неправильными учетными данными")
    @Test
    void whenLoginWithIncorrectCredentials_thenShowsError() throws Exception {
        mockMvc.perform(formLogin("/login")
                        .user(TEST_USERNAME)
                        .password("wrongpassword"))
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

    @DisplayName("должен успешно завершать сессию при логауте")
    @WithMockUser(username = "admin")
    @Test
    void whenLogout_thenSessionEnds() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(redirectedUrl("/login?logout"))
                .andExpect(unauthenticated());
    }

    @ParameterizedTest
    @MethodSource("publicUrls")
    @DisplayName("должен отображать публичные страницы без аутентификации")
    void whenAccessPublicUrlsWithoutAuth_thenReturnsPageView(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(unauthenticated());
    }

    static Stream<Arguments> protectedPageUrlsForAccess() {
        return Stream.of(
                Arguments.of("/genres"),
                Arguments.of("/genres/1/details"),
                Arguments.of("/authors"),
                Arguments.of("/authors/1/details"),
                Arguments.of("/books"),
                Arguments.of("/books/1/details"),
                Arguments.of("/books/1/comments")
        );
    }

    static Stream<Arguments> protectedPageUrls() {
        return Stream.of(
                Arguments.of("/genres"),
                Arguments.of("/genres/1/details"),
                Arguments.of("/genres/add"),
                Arguments.of("/genres/1/edit"),
                Arguments.of("/authors"),
                Arguments.of("/authors/1/details"),
                Arguments.of("/authors/add"),
                Arguments.of("/authors/1/edit"),
                Arguments.of("/books"),
                Arguments.of("/books/1/details"),
                Arguments.of("/books/add"),
                Arguments.of("/books/1/edit"),
                Arguments.of("/books/1/comments"),
                Arguments.of("/books/1/comments/1/details"),
                Arguments.of("/books/1/comments/add"),
                Arguments.of("/books/1/comments/1/edit")
        );
    }

    static Stream<Arguments> publicUrls() {
        return Stream.of(
                Arguments.of("/login"),
                Arguments.of("/favicon.ico")
        );
    }
}
package ru.otus.hw.controllers;

import org.junit.jupiter.api.BeforeEach;
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Контроллер аутентификации и авторизации ")
@Transactional
class SecurityIntegrationTest {

    private static final String TEST_USERNAME = "admin";
    private static final String TEST_PASSWORD = "admin";

    @Autowired
    private MockMvc mockMvc;

    private Map<String, String> genreFormData;
    private Map<String, String> authorFormData;
    private Map<String, String> bookFormData;
    private Map<String, String> commentFormData;

    private final Map<String, BiConsumer<String, String>> postHandlers = new HashMap<>();

    @BeforeEach
    void setUp() {
        genreFormData = new HashMap<>();
        genreFormData.put("name", "Test Genre");

        authorFormData = new HashMap<>();
        authorFormData.put("fullName", "Test Author");

        bookFormData = new HashMap<>();
        bookFormData.put("title", "Test Book");
        bookFormData.put("authorId", "1");
        bookFormData.put("genreIds", "1");

        commentFormData = new HashMap<>();
        commentFormData.put("text", "Test Comment");
        commentFormData.put("bookId", "1");

        initializePostHandlers();
    }

    @DisplayName("должен отображать форму логина без аутентификации")
    @Test
    void whenAccessLoginPageWithoutAuth_thenReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(view().name("login"))
                .andExpect(unauthenticated());
    }

    @DisplayName("должен перенаправлять на логин при попытке доступа к защищенным страницам без аутентификации")
    @MethodSource("protectedPageUrls")
    @ParameterizedTest
    void whenAccessProtectedPagesWithoutAuth_thenRedirectsToLogin(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(redirectedUrlPattern("**/login"))
                .andExpect(unauthenticated());
    }

    @DisplayName("должен отображать защищенные страницы после успешной аутентификации")
    @WithMockUser(username = "admin")
    @MethodSource("protectedPageUrlsForAccess")
    @ParameterizedTest
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

    @DisplayName("должен отображать публичные страницы без аутентификации")
    @MethodSource("publicUrls")
    @ParameterizedTest
    void whenAccessPublicUrlsWithoutAuth_thenReturnsPageView(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(unauthenticated());
    }

    @DisplayName("READER должен иметь доступ к чтению и добавлению комментариев")
    @WithMockUser(username = "reader", roles = {"READER"})
    @MethodSource("readerAllowedUrls")
    @ParameterizedTest
    void whenReaderAccessAllowedUrls_thenSuccess(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(authenticated());
                break;
            case "POST":
                if (url.contains("/comments/add")) {
                    mockMvc.perform(post(url)
                                    .param("text", commentFormData.get("text"))
                                    .param("bookId", commentFormData.get("bookId"))
                                    .with(csrf()))
                            .andExpect(status().is3xxRedirection())
                            .andExpect(authenticated());
                }
                break;
        }
    }

    @DisplayName("READER должен перенаправляться при попытке доступа к запрещенным URL")
    @WithMockUser(username = "reader", roles = {"READER"})
    @MethodSource("readerForbiddenUrls")
    @ParameterizedTest
    void whenReaderAccessForbiddenUrls_thenRedirected(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
            case "POST":
                mockMvc.perform(post(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
            case "DELETE":
                mockMvc.perform(delete(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
        }
    }

    @DisplayName("AUTHOR должен иметь доступ к созданию и редактированию контента")
    @WithMockUser(username = "author", roles = {"AUTHOR"})
    @MethodSource("authorAllowedUrls")
    @ParameterizedTest
    void whenAuthorAccessAllowedUrls_thenSuccess(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(authenticated());
                break;
            case "POST":
                performAuthorPostRequest(url);
                break;
        }
    }

    @DisplayName("AUTHOR должен перенаправляться при попытке доступа к запрещенным URL")
    @WithMockUser(username = "author", roles = {"AUTHOR"})
    @MethodSource("authorForbiddenUrls")
    @ParameterizedTest
    void whenAuthorAccessForbiddenUrls_thenRedirected(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
            case "POST":
                mockMvc.perform(post(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
            case "DELETE":
                mockMvc.perform(delete(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/access-denied"))
                        .andExpect(authenticated());
                break;
        }
    }

    @DisplayName("LIBRARIAN должен иметь доступ к созданию и редактированию контента")
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN"})
    @MethodSource("librarianAllowedUrls")
    @ParameterizedTest
    void whenLibrarianAccessAllowedUrls_thenSuccess(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(authenticated());
                break;
            case "POST":
                performLibrarianPostRequest(url);
                break;
        }
    }

    @DisplayName("LIBRARIAN должен перенаправляться при попытке доступа к запрещенным URL")
    @WithMockUser(username = "librarian", roles = {"LIBRARIAN"})
    @MethodSource("librarianForbiddenUrls")
    @ParameterizedTest
    void whenLibrarianAccessForbiddenUrls_thenRedirected(String url) throws Exception {
        mockMvc.perform(delete(url).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"))
                .andExpect(authenticated());
    }

    @DisplayName("ADMIN должен иметь полный доступ")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @ParameterizedTest
    @MethodSource("adminAllowedUrls")
    void whenAdminAccessAllowedUrls_thenSuccess(String url, String method) throws Exception {
        switch (method) {
            case "GET":
                mockMvc.perform(get(url))
                        .andExpect(status().isOk())
                        .andExpect(authenticated());
                break;
            case "POST":
                performAdminPostRequest(url);
                break;
            case "DELETE":
                mockMvc.perform(delete(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
                break;
        }
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

    static Stream<Arguments> readerAllowedUrls() {
        return Stream.of(
                Arguments.of("/genres", "GET"),
                Arguments.of("/genres/1/details", "GET"),
                Arguments.of("/authors", "GET"),
                Arguments.of("/authors/1/details", "GET"),
                Arguments.of("/books", "GET"),
                Arguments.of("/books/1/details", "GET"),
                Arguments.of("/books/1/comments", "GET"),
                Arguments.of("/books/1/comments/add", "POST")
        );
    }

    static Stream<Arguments> readerForbiddenUrls() {
        return Stream.of(
                Arguments.of("/genres/add", "POST"),
                Arguments.of("/genres/1/edit", "POST"),
                Arguments.of("/genres/1/delete", "DELETE"),
                Arguments.of("/authors/add", "POST"),
                Arguments.of("/authors/1/edit", "POST"),
                Arguments.of("/authors/1/delete", "DELETE"),
                Arguments.of("/books/add", "POST"),
                Arguments.of("/books/1/edit", "POST"),
                Arguments.of("/books/1/delete", "DELETE")
        );
    }

    static Stream<Arguments> authorAllowedUrls() {
        return Stream.of(
                Arguments.of("/genres", "GET"),
                Arguments.of("/genres/1/details", "GET"),
                Arguments.of("/genres/add", "POST"),
                Arguments.of("/genres/1/edit", "POST"),
                Arguments.of("/authors", "GET"),
                Arguments.of("/authors/1/details", "GET"),
                Arguments.of("/authors/add", "POST"),
                Arguments.of("/authors/1/edit", "POST"),
                Arguments.of("/books", "GET"),
                Arguments.of("/books/1/details", "GET"),
                Arguments.of("/books/add", "POST"),
                Arguments.of("/books/1/edit", "POST"),
                Arguments.of("/books/1/comments", "GET"),
                Arguments.of("/books/1/comments/add", "POST")
        );
    }

    static Stream<Arguments> authorForbiddenUrls() {
        return Stream.of(
                Arguments.of("/genres/1/delete", "DELETE"),
                Arguments.of("/authors/1/delete", "DELETE"),
                Arguments.of("/books/1/delete", "DELETE")
        );
    }

    static Stream<Arguments> librarianAllowedUrls() {
        return Stream.of(
                Arguments.of("/genres", "GET"),
                Arguments.of("/genres/1/details", "GET"),
                Arguments.of("/genres/add", "POST"),
                Arguments.of("/genres/1/edit", "POST"),
                Arguments.of("/authors", "GET"),
                Arguments.of("/authors/1/details", "GET"),
                Arguments.of("/authors/add", "POST"),
                Arguments.of("/authors/1/edit", "POST"),
                Arguments.of("/books", "GET"),
                Arguments.of("/books/1/details", "GET"),
                Arguments.of("/books/add", "POST"),
                Arguments.of("/books/1/edit", "POST"),
                Arguments.of("/books/1/comments", "GET"),
                Arguments.of("/books/1/comments/add", "POST")
        );
    }

    static Stream<Arguments> librarianForbiddenUrls() {
        return Stream.of(
                Arguments.of("/genres/1/delete"),
                Arguments.of("/authors/1/delete"),
                Arguments.of("/books/1/delete")
        );
    }

    static Stream<Arguments> adminAllowedUrls() {
        return Stream.of(
                Arguments.of("/genres", "GET"),
                Arguments.of("/genres/1/details", "GET"),
                Arguments.of("/genres/add", "POST"),
                Arguments.of("/genres/1/edit", "POST"),
                Arguments.of("/genres/1/delete", "DELETE"),
                Arguments.of("/authors", "GET"),
                Arguments.of("/authors/1/details", "GET"),
                Arguments.of("/authors/add", "POST"),
                Arguments.of("/authors/1/edit", "POST"),
                Arguments.of("/authors/1/delete", "DELETE"),
                Arguments.of("/books", "GET"),
                Arguments.of("/books/1/details", "GET"),
                Arguments.of("/books/add", "POST"),
                Arguments.of("/books/1/edit", "POST"),
                Arguments.of("/books/1/delete", "DELETE"),
                Arguments.of("/books/1/comments", "GET"),
                Arguments.of("/books/1/comments/add", "POST"),
                Arguments.of("/books/1/comments/1/edit", "POST"),
                Arguments.of("/books/1/comments/1/delete", "DELETE")
        );
    }

    private void initializePostHandlers() {
        postHandlers.put("genreAdd", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("name", genreFormData.get("name"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("genreEdit", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("name", genreFormData.get("name"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("authorAdd", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("fullName", authorFormData.get("fullName"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("authorEdit", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("fullName", authorFormData.get("fullName"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("bookAdd", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("title", bookFormData.get("title"))
                                .param("authorId", bookFormData.get("authorId"))
                                .param("genreIds", bookFormData.get("genreIds"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("bookEdit", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("title", bookFormData.get("title"))
                                .param("authorId", bookFormData.get("authorId"))
                                .param("genreIds", bookFormData.get("genreIds"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("commentAdd", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("text", commentFormData.get("text"))
                                .param("bookId", commentFormData.get("bookId"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("commentEdit", (url, role) -> {
            try {
                mockMvc.perform(post(url)
                                .param("text", commentFormData.get("text"))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        postHandlers.put("default", (url, role) -> {
            try {
                mockMvc.perform(post(url).with(csrf()))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(authenticated());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private String determineHandlerKey(String url) {
        if (url.contains("/genres/add")) return "genreAdd";
        if (url.contains("/genres/") && url.contains("/edit")) return "genreEdit";
        if (url.contains("/authors/add")) return "authorAdd";
        if (url.contains("/authors/") && url.contains("/edit")) return "authorEdit";
        if (url.contains("/books/add")) return "bookAdd";
        if (url.contains("/books/") && url.contains("/edit") && !url.contains("/comments")) return "bookEdit";
        if (url.contains("/comments/add")) return "commentAdd";
        if (url.contains("/comments/") && url.contains("/edit")) return "commentEdit";
        return "default";
    }

    private void performAuthorPostRequest(String url) {
        String handlerKey = determineHandlerKey(url);
        BiConsumer<String, String> handler = postHandlers.getOrDefault(handlerKey, postHandlers.get("default"));
        handler.accept(url, "AUTHOR");
    }

    private void performLibrarianPostRequest(String url) {
        performAuthorPostRequest(url);
    }

    private void performAdminPostRequest(String url) throws Exception {
        if (url.contains("/genres/") && url.contains("/delete")) {
            mockMvc.perform(delete(url).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(authenticated());
        } else if (url.contains("/authors/") && url.contains("/delete")) {
            mockMvc.perform(delete(url).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(authenticated());
        } else if (url.contains("/books/") && url.contains("/delete")) {
            mockMvc.perform(delete(url).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(authenticated());
        } else if (url.contains("/comments/") && url.contains("/delete")) {
            mockMvc.perform(delete(url).with(csrf()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(authenticated());
        } else {
            String handlerKey = determineHandlerKey(url);
            BiConsumer<String, String> handler = postHandlers.getOrDefault(handlerKey, postHandlers.get("default"));
            handler.accept(url, "ADMIN");
        }
    }
}
package ru.pricat.controller.web;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.pricat.exception.UserNotFoundException;
import ru.pricat.model.dto.auth.LoginRequestDto;
import ru.pricat.model.dto.auth.LoginResponseDto;
import ru.pricat.model.dto.auth.RegisterRequestDto;
import ru.pricat.model.dto.auth.UserResponseDto;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.service.AuthService;
import ru.pricat.service.ClientService;

import java.util.List;

/**
 * Web Controller для управления клиентскими профилями через Thymeleaf.
 * Обрабатывает HTTP запросы для страниц логина, регистрации, профиля и админ-панели.
 * Работает с куками для хранения access и refresh токенов.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("")
public class ClientWebController {

    private static final String ACCESS_TOKEN_COOKIE = "access-token";

    private static final String REFRESH_TOKEN_COOKIE = "refresh-token";

    private static final int COOKIE_MAX_AGE = 60 * 60;

    private final AuthService authService;

    private final ClientService clientService;

    /**
     * Отображает страницу логина.
     *
     * @param error параметр ошибки аутентификации
     * @param loggedOut параметр успешного выхода
     * @param session_expired параметр истечения сессии
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы логина
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(required = false) String error,
                                @RequestParam(required = false) String loggedOut,
                                @RequestParam(required = false) String session_expired,
                                Model model) {
        if ("true".equals(error)) {
            model.addAttribute("error", "Login failed");
        }
        if ("true".equals(loggedOut)) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        if ("true".equals(session_expired)) {
            model.addAttribute("error", "Your session has expired. Please login again.");
        }
        model.addAttribute("loginRequest", new LoginRequestDto("", ""));
        return "login";
    }

    /**
     * Отображает страницу регистрации.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы регистрации
     */
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("registerRequest", new RegisterRequestDto("", "", ""));
        return "register";
    }

    /**
     * Отображает страницу профиля текущего пользователя.
     *
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы профиля
     * @throws RuntimeException если пользователь не аутентифицирован
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            log.warn("Attempt to access profile without authentication");
            return "redirect:/login";
        }
        String username = authentication.getName();
        log.info("Getting current user profile for: {}", username);
        try {
            AdminProfileDto client = clientService.getCurrentUserProfile(username);
            model.addAttribute("user", client);
            return "profile";
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", username);
            return "redirect:/login?error=user_not_found";
        }
    }

    /**
     * Отображает админ-панель (доступно только пользователям с ролью ADMIN).
     *
     * @param request HTTP запрос для получения куков
     * @param model модель для передачи данных в представление
     * @return имя шаблона админ-панели или редирект на логин при отсутствии доступа
     */
    @GetMapping("/admin")
    public String showAdminPage(HttpServletRequest request, Model model) {
        String accessToken = getAccessTokenFromCookie(request);
        if (accessToken == null) {
            return "redirect:/login";
        }
        try {
            UserResponseDto user = authService.getMe(accessToken);
            if (!hasRole(user.roles())) {
                log.warn("Endpoint '/admin' - access denied for non-admin user: {}", user.username());
                return "redirect:/login";
            }
            model.addAttribute("user", user);
            return "admin";
        } catch (Exception e) {
            log.error("Error accessing admin page", e);
            return "redirect:/login";
        }
    }

    /**
     * Отображает страницу пользователя (доступно только администраторам).
     *
     * @param username имя пользователя для просмотра
     * @param request HTTP запрос для получения куков
     * @param model модель для передачи данных в представление
     * @return имя шаблона страницы пользователя или редирект на логин при отсутствии доступа
     */
    @GetMapping("/user/{username}")
    public String showUser(@PathVariable String username,
                           HttpServletRequest request,
                           Model model) {
        String accessToken = getAccessTokenFromCookie(request);
        if (accessToken == null) {
            return "redirect:/login";
        }
        try {
            UserResponseDto currentUser = authService.getMe(accessToken);
            if (!hasRole(currentUser.roles())) {
                log.warn("Endpoint '/user/{username}' - access denied for non-admin user: {}", currentUser.username());
                return "redirect:/login";
            }
            UserResponseDto user = authService.getUser(username, accessToken);
            model.addAttribute("user", user);
            return "user";
        } catch (Exception e) {
            log.error("Error accessing user page for: {}", username, e);
            return "redirect:/login";
        }
    }

    /**
     * Обрабатывает запрос на аутентификацию пользователя.
     *
     * @param request DTO с данными для входа
     * @param result результаты валидации
     * @param response HTTP ответ для установки куков
     * @return редирект на страницу профиля при успехе или страницу логина при ошибке
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginRequest") LoginRequestDto request,
                        BindingResult result,
                        HttpServletResponse response) {
        if (result.hasErrors()) {
            return "login";
        }
        try {
            LoginResponseDto loginResponse = authService.login(request);
            addCookie(response, loginResponse.accessToken());
            addHttpOnlyCookie(response, loginResponse.refreshToken());
            log.info("Successfully logged in user: {}", request.username());
            return "redirect:/profile";
        } catch (Exception e) {
            log.error("Login failed for user: {}", request.username(), e);
            return "redirect:/login?error=true";
        }
    }

    /**
     * Обрабатывает запрос на регистрацию нового пользователя.
     *
     * @param request DTO с данными для регистрации
     * @param result результаты валидации
     * @param model модель для передачи данных в представление
     * @return редирект на страницу логина при успехе или страницу регистрации при ошибке
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") RegisterRequestDto request,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            authService.register(request);
            log.info("Successfully registered user: {}", request.username());
            model.addAttribute("success", "Registration successful! Please login.");
            return "redirect:/login";
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.username(), e);
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    /**
     * Обрабатывает запрос на выход пользователя из системы.
     *
     * @param request HTTP запрос для получения куков
     * @param response HTTP ответ для очистки куков
     * @return редирект на страницу логина с параметром loggedOut
     */
    @PostMapping("/logout")
    public String logout(HttpServletRequest request,
                         HttpServletResponse response) {
        String accessToken = getAccessTokenFromCookie(request);
        if (accessToken != null) {
            try {
                authService.logout(accessToken);
                log.info("Successfully logged out user");
            } catch (Exception e) {
                log.warn("Error during logout, but clearing cookies anyway", e);
            }
        }
        clearCookie(response, ACCESS_TOKEN_COOKIE);
        clearCookie(response, REFRESH_TOKEN_COOKIE);
        SecurityContextHolder.clearContext();
        return "redirect:/login?loggedOut=true";
    }

    /**
     * Отображает страницу редактирования профиля.
     * Для администраторов позволяет редактировать любой профиль по параметру username.
     * Для обычных пользователей редактирует только свой профиль.
     *
     * @param username имя пользователя для редактирования (опционально, для администраторов)
     * @return имя шаблона страницы редактирования профиля
     */
    @GetMapping("/profile-edit")
    public String showProfileEdit(@RequestParam(required = false) String username,
                                  Authentication authentication,
                                  Model model) {
        log.debug("Profile-edit form render called for user: {}", username);
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        String currentUser = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        String targetUsername = (username != null && isAdmin) ? username : currentUser;
        try {
            AdminProfileDto client = clientService.getUserProfile(targetUsername);
            model.addAttribute("client", client);
            model.addAttribute("isEditingOtherUser", isAdmin && !targetUsername.equals(currentUser));
            model.addAttribute("isAdmin", isAdmin);
            return "profile-edit";
        } catch (UserNotFoundException e) {
            log.warn("User not found for editing: {}", targetUsername);
            return "redirect:/clients?error=User not found";
        }
    }

    /**
     * Отображает страницу списка клиентов. Доступна только для роли ADMIN.
     *
     * @return имя шаблона страницы списка клиентов
     */
    @GetMapping("/clients")
    public String showClientsPage(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            log.warn("Access denied to clients page for non-admin user: {}", authentication.getName());
            return "redirect:/profile?warning=access_denied_to_clients";
        }
        return "clients";
    }

    private String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, ACCESS_TOKEN_COOKIE);
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, REFRESH_TOKEN_COOKIE);
    }

    @Nullable
    private String getCookieValue(@NonNull HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void addCookie(@NonNull HttpServletResponse response, String value) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(ClientWebController.ACCESS_TOKEN_COOKIE,
                value);
        cookie.setPath("/");
        cookie.setMaxAge(ClientWebController.COOKIE_MAX_AGE);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
    }

    private void addHttpOnlyCookie(@NonNull HttpServletResponse response, String value) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(ClientWebController.REFRESH_TOKEN_COOKIE,
                value);
        cookie.setPath("/");
        cookie.setMaxAge(604800);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void clearCookie(@NonNull HttpServletResponse response, String name) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private boolean hasRole(List<String> userRoles) {
        return userRoles != null && userRoles.contains("ADMIN");
    }
}

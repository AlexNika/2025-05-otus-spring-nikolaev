package ru.pricat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.pricat.exception.EmailNotUniqueException;
import ru.pricat.exception.ErrorResponse;
import ru.pricat.exception.InvalidCredentialsException;
import ru.pricat.exception.InvalidRefreshTokenException;
import ru.pricat.exception.LastRoleRemovalException;
import ru.pricat.exception.RoleNotFoundException;
import ru.pricat.exception.UserAlreadyExistsException;
import ru.pricat.exception.UserNotFoundException;
import ru.pricat.model.User;
import ru.pricat.model.dto.request.AddRoleRequestDto;
import ru.pricat.model.dto.request.AdminChangePasswordRequestDto;
import ru.pricat.model.dto.request.ChangePasswordRequestDto;
import ru.pricat.model.dto.request.LoginRequestDto;
import ru.pricat.model.dto.response.LoginResponseDto;
import ru.pricat.model.dto.request.RefreshTokenRequestDto;
import ru.pricat.model.dto.request.RegisterRequestDto;
import ru.pricat.model.dto.request.RemoveRoleRequestDto;
import ru.pricat.model.dto.UserProfileDto;
import ru.pricat.service.AuthService;
import ru.pricat.service.TokenBlacklistService;

import java.security.Principal;

import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;

/**
 * REST-контроллер для обработки запросов, связанных с аутентификацией, авторизацией и управлением пользователями.
 * Предоставляет API для логина, регистрации, обновления токенов, выхода из системы,
 * а также для административного управления пользователями и ролями.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(API_V1_AUTH_PATH)
@Tag(name = "Authentication Controller", description = "APIs for user authentication, registration, logout and refresh")
public class AuthController {

    /**
     * Сервис аутентификации и управления пользователями.
     */
    private final AuthService authService;

    /**
     * Сервис для управления черным списком токенов.
     */
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Реактивный декодер JWT, используемый для извлечения информации из токена при логауте.
     */
    private final ReactiveJwtDecoder jwtDecoder;

    /**
     * Аутентифицирует пользователя по имени пользователя и паролю.
     * Возвращает DTO с access и refresh токенами при успешной аутентификации.
     *
     * @param loginRequestDto DTO с данными для логина (имя пользователя, пароль)
     * @return реактивный объект с ResponseEntity, содержащим DTO ответа на логин
     * @throws InvalidCredentialsException если имя пользователя или пароль неверны
     */
    @Operation(summary = "Authenticate user",
            description = "Logs in a user and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDto>> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
            )
            @Valid @RequestBody LoginRequestDto loginRequestDto) {
        log.info("Login attempt for user: {}", loginRequestDto.username());
        return authService.authenticate(loginRequestDto.username(), loginRequestDto.password())
                .map(this::createSuccessResponse)
                .doOnSuccess(_ -> log.info("Login successful for user: {}",
                        loginRequestDto.username()))
                .doOnError(error -> log.error("Login failed for user: {}", loginRequestDto.username(), error));
    }

    /**
     * Обновляет пару access/refresh токенов на основе существующего refresh-токена.
     * Refresh-токен может быть передан в заголовке X-Refresh-Token или в теле запроса.
     *
     * @param refreshTokenRequestDto DTO с refresh-токеном (опционально, если токен в заголовке)
     * @param exchange               объект обмена Spring WebFlux для доступа к заголовкам
     * @return реактивный объект с ResponseEntity, содержащим DTO с новыми токенами
     * @throws InvalidRefreshTokenException если refresh-токен отсутствует, недействителен или истек
     */
    @Operation(summary = "Refresh access token",
            description = "Exchanges a valid refresh token for a new access token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public Mono<ResponseEntity<LoginResponseDto>> refresh(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Refresh token request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RefreshTokenRequestDto.class))
            )
            @RequestBody(required = false) RefreshTokenRequestDto refreshTokenRequestDto, ServerWebExchange exchange) {
        String refreshToken = exchange.getRequest().getHeaders().getFirst("X-Refresh-Token");
        if (refreshToken == null && refreshTokenRequestDto != null) {
            refreshToken = refreshTokenRequestDto.refreshToken();
        }
        if (refreshToken == null) {
            log.warn("Refresh token request failed: no refresh token provided in header or body.");
            throw new InvalidRefreshTokenException("Refresh token is missing");
        }
        log.info("Refresh token request received");
        return authService.refreshToken(refreshToken)
                .map(this::createSuccessResponse)
                .doOnSuccess(_ -> log.info("Token refreshed successfully"))
                .doOnError(error -> log.error("Token refresh failed", error));
    }

    /**
     * Регистрирует нового пользователя.
     * Проверяет уникальность имени пользователя и email.
     *
     * @param registerRequestDto DTO с данными для регистрации (имя пользователя, email, пароль)
     * @return реактивный объект с созданным пользователем
     * @throws UserAlreadyExistsException если имя пользователя или email уже заняты
     * @throws EmailNotUniqueException    если email уже используется другим пользователем
     */
    @Operation(summary = "Register a new user",
            description = "Creates a new user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "409", description = "User already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/register")
    public Mono<User> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = RegisterRequestDto.class))
            )
            @Valid @RequestBody RegisterRequestDto registerRequestDto) {
        log.info("Registration attempt for user: {}", registerRequestDto.username());
        return authService.register(registerRequestDto)
                .doOnSuccess(savedUser -> log.info("User registered successfully: {}", savedUser.getUsername()))
                .doOnError(error -> log.error("Registration failed for user: {}", registerRequestDto.username(),
                        error))
                .onErrorResume(UserAlreadyExistsException.class, ex -> {
                    log.warn("Registration failed due to existing username: {}", registerRequestDto.username());
                    return Mono.error(ex);
                })
                .onErrorResume(EmailNotUniqueException.class, ex -> {
                    log.warn("Registration failed due to existing email: {}", registerRequestDto.email());
                    return Mono.error(ex);
                });
    }

    /**
     * Выполняет logout текущего пользователя, добавляя его JWT-токен в черный список.
     * Токен извлекается из заголовка Authorization запроса.
     *
     * @param exchange объект обмена Spring WebFlux для доступа к заголовкам
     * @return реактивный объект с ResponseEntity, сигнализирующий о завершении операции
     */
    @Operation(summary = "Logout user",
            description = "Invalidates the current JWT token by adding it to the blacklist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "401", description = "Token is invalid or blacklisted")
    })
    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        log.info("Logout request received.");
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            log.debug("Attempting to blacklist token: {}", token);
            return jwtDecoder.decode(token)
                    .doOnNext(jwt -> {
                        String jti = jwt.getId();
                        log.debug("Extracted jti '{}' from token for blacklisting.", jti);
                        tokenBlacklistService.blacklistToken(jti);
                    })
                    .doOnSuccess(_ -> log.info("Token successfully blacklisted."))
                    .doOnError(error ->
                            log.warn("Failed to decode token for logout (likely expired or invalid): {}",
                                    error.getMessage()))
                    .then(Mono.just(ResponseEntity.ok().build()));
        } else {
            log.warn("Logout request received without valid Bearer token.");
        }
        return Mono.just(ResponseEntity.ok().build());
    }


    /**
     * Возвращает простое сообщение, доступное пользователям с ролью USER или ADMIN.
     * Использует аннотацию @PreAuthorize для проверки прав доступа. Используется для тестирования.
     *
     * @return строка с сообщением
     */
    @Operation(summary = "Get user info",
            description = "Returns a simple message accessible to users with 'USER' or 'ADMIN' roles.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public Mono<String> getUserInfo() {
        log.debug("Accessing user info endpoint");
        return Mono.just("User info endpoint - accessible to both USER and ADMIN");
    }

    /**
     * Возвращает простое сообщение, доступное только пользователям с ролью ADMIN.
     * Использует аннотацию @PreAuthorize для проверки прав доступа. Используется для тестирования.
     *
     * @return строка с сообщением
     */
    @Operation(summary = "Get admin info", description = "Returns a simple message accessible only to users with " +
                                                         "'ADMIN' role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin info retrieved",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<String> getAdminInfo() {
        log.debug("Accessing admin info endpoint");
        return Mono.just("Admin info endpoint - only for ADMIN");
    }

    /**
     * Возвращает профиль текущего аутентифицированного пользователя.
     * Извлекает имя пользователя из Principal.
     *
     * @param exchange объект обмена Spring WebFlux для доступа к Principal
     * @return реактивный объект с DTO профиля пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Get current user profile",
            description = "Returns the profile of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<UserProfileDto> getCurrentUser(ServerWebExchange exchange) {
        log.debug("Accessing current user profile endpoint");
        return exchange.getPrincipal()
                .cast(Principal.class)
                .map(Principal::getName)
                .flatMap(authService::getCurrentUserProfile)
                .doOnSuccess(profile -> log.info("Current user profile retrieved successfully for: {}",
                        profile.username()))
                .doOnError(error -> log.error("Failed to retrieve current user profile", error));
    }

    /**
     * Возвращает профиль пользователя по его имени пользователя.
     * Доступно только администратору.
     *
     * @param username имя пользователя, чей профиль нужно получить
     * @return реактивный объект с DTO профиля пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Get user profile by username",
            description = "Returns the profile of a specific user by username. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserProfileDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<UserProfileDto> getUserProfileByUsername(@PathVariable String username) {
        log.info("Get user profile request for username: {}", username);
        return authService.getUserProfileByUsername(username)
                .doOnSuccess(_ -> log.info("User profile retrieved successfully for: {}", username))
                .doOnError(error -> log.error("Failed to retrieve user profile for username: {}", username, error))
                .onErrorResume(UserNotFoundException.class, ex -> {
                    log.warn("Get user profile failed: user not found - {}", username);
                    throw ex;
                });
    }

    /**
     * Удаляет пользователя по его имени пользователя.
     * Доступно только администратору.
     *
     * @param username имя пользователя для удаления
     * @return реактивный объект с ResponseEntity, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Delete user by username",
            description = "Deletes a user by username. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String username) {
        log.info("Delete user request received for username: {}", username);
        return authService.deleteUser(username)
                .then(Mono.fromCallable(() -> ResponseEntity.noContent().<Void>build()))
                .doOnSuccess(_ -> log.info("User deleted successfully: {}", username))
                .doOnError(error -> log.error("Delete user failed for username: {}", username, error))
                .onErrorResume(UserNotFoundException.class, _ -> {
                    log.warn("Delete user failed: user not found - {}", username);
                    return Mono.fromCallable(() -> ResponseEntity.notFound().build());
                });
    }

    /**
     * Включает учетную запись пользователя по его имени пользователя.
     * Доступно только администратору.
     *
     * @param username имя пользователя для включения
     * @return реактивный объект с обновленным пользователем
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Enable user by username",
            description = "Enables a user account by username. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User enabled successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/user/{username}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<User> enableUser(@PathVariable String username) {
        log.info("Enable user request received for username: {}", username);
        return authService.updateUserEnabledStatus(username, true)
                .doOnSuccess(_ -> log.info("User enabled successfully: {}", username))
                .doOnError(error -> log.error("Enable user failed for username: {}", username, error));
    }

    /**
     * Отключает учетную запись пользователя по его имени пользователя.
     * Доступно только администратору.
     *
     * @param username имя пользователя для отключения
     * @return реактивный объект с обновленным пользователем
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Disable user by username",
            description = "Disables a user account by username. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User disabled successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/user/{username}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<User> disableUser(@PathVariable String username) {
        log.info("Disable user request received for username: {}", username);
        return authService.updateUserEnabledStatus(username, false)
                .doOnSuccess(_ -> log.info("User disabled successfully: {}", username))
                .doOnError(error -> log.error("Disable user failed for username: {}", username, error));
    }

    /**
     * Добавляет роль пользователю по его имени пользователя.
     * Доступно только администратору. Если пользователь уже имеет роль, операция игнорируется.
     *
     * @param username            имя пользователя
     * @param addRoleRequestDto DTO с названием роли для добавления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     * @throws RoleNotFoundException если роль не найдена
     */
    @Operation(summary = "Add a role to a user",
            description = "Adds a specified role to a user. Requires ADMIN role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid role name or request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/user/{username}/add-role")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> addRole(@PathVariable String username,
                              @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                      description = "Add role data",
                                      required = true,
                                      content = @Content(schema = @Schema(implementation = AddRoleRequestDto.class))
                              )
                              @Valid @RequestBody AddRoleRequestDto addRoleRequestDto) {
        log.info("Add role '{}' request received for user: {}", addRoleRequestDto.role(), username);
        return authService.addRoleToUser(username, addRoleRequestDto.role())
                .doOnSuccess(_ -> log.info("Role '{}' added successfully to user: {}", addRoleRequestDto.role(),
                        username))
                .doOnError(error -> log.error("Failed to add role '{}' to user: {}", addRoleRequestDto.role(),
                        username, error))
                .onErrorResume(UserNotFoundException.class, ex -> {
                    log.warn("Add role failed: user not found - {}", username);
                    throw ex;
                })
                .onErrorResume(RoleNotFoundException.class, ex -> {
                    log.warn("Add role failed: role not found - {}", addRoleRequestDto.role());
                    throw ex;
                });
    }

    /**
     * Удаляет роль у пользователя по его имени пользователя.
     * Доступно только администратору. Предотвращает удаление последней роли пользователя.
     *
     * @param username               имя пользователя
     * @param removeRoleRequestDto DTO с названием роли для удаления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException     если пользователь не найден
     * @throws RoleNotFoundException     если роль не найдена
     * @throws LastRoleRemovalException если попытка удалить последнюю роль пользователя
     */
    @Operation(summary = "Remove a role from a user",
            description = "Removes a specified role from a user. Requires ADMIN role. Cannot remove the last role.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role removed successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid role name, request body, or trying to remove the last role",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User or role not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/user/{username}/remove-role")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> removeRole(@PathVariable String username,
                                 @io.swagger.v3.oas.annotations.parameters.RequestBody(
                                         description = "Remove role data",
                                         required = true,
                                         content = @Content(schema = @Schema(implementation = RemoveRoleRequestDto.class))
                                 )
                                 @Valid @RequestBody RemoveRoleRequestDto removeRoleRequestDto) {
        log.info("Remove role '{}' request received for user: {}", removeRoleRequestDto.role(), username);
        return authService.removeRoleFromUser(username, removeRoleRequestDto.role())
                .doOnSuccess(_ -> log.info("Role '{}' removed successfully from user: {}",
                        removeRoleRequestDto.role(), username))
                .doOnError(error -> log.error("Failed to remove role '{}' from user: {}",
                        removeRoleRequestDto.role(), username, error))
                .onErrorResume(UserNotFoundException.class, ex -> {
                    log.warn("Remove role failed: user not found - {}", username);
                    throw ex;
                })
                .onErrorResume(RoleNotFoundException.class, ex -> {
                    log.warn("Remove role failed: role not found - {}", removeRoleRequestDto.role());
                    throw ex;
                })
                .onErrorResume(LastRoleRemovalException.class, ex -> {
                    log.warn("Attention, user has last role! Remove role failed: {}", ex.getMessage());
                    throw ex;
                });
    }

    /**
     * Изменяет пароль текущего аутентифицированного пользователя.
     * Извлекает имя пользователя из Principal.
     *
     * @param changePasswordRequestDto DTO с текущим и новым паролем
     * @param exchange                 объект обмена Spring WebFlux для доступа к Principal
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws InvalidCredentialsException если старый пароль неверен
     * @throws IllegalArgumentException    если новый пароль совпадает со старым или не соответствует требованиям
     */
    @Operation(summary = "Change current user's password",
            description = "Changes the password of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request body (e.g., old/new password mismatch, weak new password)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized (invalid old password)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (user is not authenticated)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public Mono<Void> changePassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Change password data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ChangePasswordRequestDto.class))
            )
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto,
            ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .cast(Principal.class)
                .map(Principal::getName)
                .flatMap(username -> {
                    log.info("Change password request received for user: {}", username);
                    return authService.changeCurrentUserPassword(username, changePasswordRequestDto);
                })
                .doOnSuccess(_ -> log.info("Password changed successfully for user"))
                .doOnError(error -> log.error("Failed to change password for current user", error))
                .onErrorResume(InvalidCredentialsException.class, ex -> {
                    log.warn("Change password failed: old password is incorrect");
                    throw ex;
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("Change password failed: {}", ex.getMessage());
                    throw ex;
                });
    }

    /**
     * Позволяет администратору изменить пароль любого пользователя.
     * Также инвалидирует все refresh-токены пользователя.
     *
     * @param username                          имя пользователя, пароль которого нужно изменить
     * @param adminChangePasswordRequestDto DTO с новым паролем
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     */
    @Operation(summary = "Change user's password by admin",
            description = "Allows an admin to change the password of any user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body (e.g., weak new password)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden (not an admin)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/user/{username}/change-password")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Void> changeUserPasswordByAdmin(
            @PathVariable String username,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Admin change password data",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AdminChangePasswordRequestDto.class))
            )
            @Valid @RequestBody AdminChangePasswordRequestDto adminChangePasswordRequestDto) {
        log.info("Admin initiated password change request for user: {}", username);
        return authService.changeUserPasswordByAdmin(username, adminChangePasswordRequestDto)
                .doOnSuccess(_ -> log.info("Password successfully changed for user: {} by an admin.", username))
                .doOnError(error -> log.error("Admin password change failed for user: {}", username, error))
                .onErrorResume(UserNotFoundException.class, ex -> {
                    log.warn("Admin password change failed: user not found - {}", username);
                    throw ex;
                });
    }

    /**
     * Создаёт успешный ResponseEntity с LoginResponseDto и X-Refresh-Token в заголовке.
     * Refresh-токен передаётся в заголовке X-Refresh-Token, а не в теле ответа.
     *
     * @param loginResponseDto DTO с токенами
     * @return ResponseEntity с телом без refresh-токена и заголовком X-Refresh-Token
     */
    private ResponseEntity<LoginResponseDto> createSuccessResponse(LoginResponseDto loginResponseDto) {
        LoginResponseDto responseWithoutRefresh = new LoginResponseDto(
                loginResponseDto.accessToken(),
                null,
                loginResponseDto.tokenType(),
                loginResponseDto.expiresIn()
        );
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Refresh-Token", loginResponseDto.refreshToken());
        return ResponseEntity.ok().headers(headers).body(responseWithoutRefresh);
    }
}

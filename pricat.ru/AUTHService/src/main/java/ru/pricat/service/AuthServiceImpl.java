package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.pricat.config.properties.JwtSecretKeyConfig;
import ru.pricat.config.properties.RefreshTokenConfig;
import ru.pricat.exception.EmailNotUniqueException;
import ru.pricat.exception.InvalidCredentialsException;
import ru.pricat.exception.LastRoleRemovalException;
import ru.pricat.exception.RoleNotFoundException;
import ru.pricat.exception.UserAlreadyExistsException;
import ru.pricat.exception.UserDisabledException;
import ru.pricat.exception.UserNotFoundException;
import ru.pricat.model.Role;
import ru.pricat.model.User;
import ru.pricat.model.UserRefreshToken;
import ru.pricat.model.dto.request.AdminChangePasswordRequestDto;
import ru.pricat.model.dto.request.ChangePasswordRequestDto;
import ru.pricat.model.dto.response.LoginResponseDto;
import ru.pricat.model.dto.request.RegisterRequestDto;
import ru.pricat.model.dto.UserProfileDto;
import ru.pricat.repository.RoleRepository;
import ru.pricat.repository.UserRefreshTokenRepository;
import ru.pricat.repository.UserRepository;
import ru.pricat.repository.UserRoleRepository;
import ru.pricat.util.JwtUtil;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Реализация сервиса аутентификации и управления пользователями.
 * Обрабатывает логин, регистрацию, обновление токенов, управление ролями и профилями пользователей.
 * Использует реактивное программирование (Project Reactor) для асинхронной обработки.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthServiceImpl implements AuthService {

    private final JwtSecretKeyConfig jwtSecretKeyConfig;

    private final RefreshTokenConfig refreshTokenConfig;

    private static final String TOKEN_TYPE = "Bearer";

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;

    private final UserRoleRepository userRoleRepository;

    private final UserRefreshTokenRepository userRefreshTokenRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final LoginAttemptService loginAttemptService;

    private final WebClientService webClientService;

    /**
     * Аутентифицирует пользователя по имени пользователя и паролю.
     * Проверяет, не заблокирован ли пользователь, существует ли он, включена ли его учетная запись,
     * и корректен ли пароль. При успешной аутентификации генерирует новые access и refresh токены.
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return реактивный объект с DTO, содержащим access и refresh токены
     * @throws InvalidCredentialsException если пользователь не найден, пароль неверен или учетная запись заблокирована
     * @throws UserDisabledException       если учетная запись пользователя отключена
     */
    @Override
    public Mono<LoginResponseDto> authenticate(String username, String password) {
        log.debug("Authenticating user: {}", username);
        if (loginAttemptService.isBlocked(username)) {
            log.warn("User {} is temporarily blocked due to too many failed login attempts", username);
            return Mono.error(new InvalidCredentialsException("Too many failed login attempts. Try again later."));
        }
        return userRepository.findByUsernameWithRoles(username)
                .switchIfEmpty(Mono.error(() -> {
                    loginAttemptService.loginFailed(username);
                    return new InvalidCredentialsException("User not found");
                }))
                .filter(user -> {
                    if (!user.getEnabled()) {
                        log.warn("Authentication failed: user '{}' is disabled.", username);
                        throw new UserDisabledException("User account is disabled");
                    }
                    if (!user.getIsProfileCreated()) {
                        log.warn("Authentication failed: user '{}' has no profile created in client-service.",
                                username);
                        throw new InvalidCredentialsException("User profile is not yet created. Contact support.");
                    }
                    return true;
                })
                .filter(user -> {
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        loginAttemptService.loginFailed(username);
                        log.warn("Invalid password for user: {}", username);
                    }
                    return matches;
                })
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
                .doOnNext(_ -> loginAttemptService.loginSucceeded(username))
                .flatMap(user -> userRefreshTokenRepository.deleteByUserId(user.getId())
                        .then(Mono.just(user)))
                .flatMap(user -> {
                    String refreshToken = generateRefreshToken();
                    Instant expiresAt = Instant.now().plusSeconds(refreshTokenConfig.getRefreshTokenMaxAge());
                    UserRefreshToken userRefreshToken = new UserRefreshToken(user.getId(), refreshToken, expiresAt);
                    return userRefreshTokenRepository.save(userRefreshToken)
                            .map(savedTokenEntity -> {
                                log.info("User authenticated successfully: {}", username);
                                String accessToken = jwtUtil.generateToken(user.getUsername(), user.getRoles());
                                return new LoginResponseDto(accessToken, savedTokenEntity.getToken(), TOKEN_TYPE,
                                        jwtSecretKeyConfig.getAuthTokenMaxAge());
                            });
                });
    }

    /**
     * Обновляет пару access/refresh токенов на основе существующего refresh-токена.
     * Проверяет, действителен ли переданный refresh-токен и не истек ли срок его действия.
     * При успешной проверке генерирует новый refresh-токен и новый access-токен.
     *
     * @param refreshToken текущий refresh-токен пользователя
     * @return реактивный объект с DTO, содержащим новые access и refresh токены
     * @throws InvalidCredentialsException если токен недействителен или срок его действия истек
     */
    @Override
    public Mono<LoginResponseDto> refreshToken(String refreshToken) {
        log.debug("Refreshing token for token: {}", refreshToken);
        return userRefreshTokenRepository.findByToken(refreshToken)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid refresh token")))
                .flatMap(tokenEntity -> {
                    if (tokenEntity.getExpiresAt().isAfter(Instant.now())) {
                        return userRepository.findById(tokenEntity.getUserId())
                                .switchIfEmpty(Mono.error(new InvalidCredentialsException("User associated with " +
                                                                                          "refresh token not found")))
                                .map(user -> new RefreshTokenValidationResult(tokenEntity, user));
                    } else {
                        log.warn("Attempt to refresh with expired token: {}", refreshToken);
                        return userRefreshTokenRepository.deleteById(tokenEntity.getId())
                                .then(Mono.error(new InvalidCredentialsException("Refresh token expired")));
                    }
                })
                .flatMap(validationResult -> {
                    UserRefreshToken tokenEntity = validationResult.tokenEntity();
                    User user = validationResult.user();
                    String newRefreshToken = generateRefreshToken();
                    Instant newExpiresAt = Instant.now().plusSeconds(refreshTokenConfig.getRefreshTokenMaxAge());
                    UserRefreshToken newTokenEntity = new UserRefreshToken(user.getId(), newRefreshToken, newExpiresAt);
                    return userRefreshTokenRepository.deleteById(tokenEntity.getId())
                            .then(userRefreshTokenRepository.save(newTokenEntity))
                            .then(userRepository.findByUsernameWithRoles(user.getUsername()))
                            .switchIfEmpty(Mono.error(new RuntimeException("User roles not found for user: " +
                                                                           user.getUsername())))
                            .map(userWithRoles -> {
                                String newAccessToken = jwtUtil.generateToken(userWithRoles.getUsername(),
                                        userWithRoles.getRoles());
                                log.info("Token refreshed successfully for user: {}", user.getUsername());
                                return new LoginResponseDto(newAccessToken, newRefreshToken, TOKEN_TYPE,
                                        jwtSecretKeyConfig.getAuthTokenMaxAge());
                            });
                });
    }

    /**
     * Регистрирует нового пользователя.
     * Проверяет уникальность email и username. Сохраняет пользователя в базе данных,
     * создает профиль в client-service (с откатом при ошибке), и присваивает ему роль 'USER'.
     *
     * @param registerRequestDto DTO с данными для регистрации (имя пользователя, email, пароль)
     * @return реактивный объект с созданным пользователем
     * @throws EmailNotUniqueException    если email уже используется другим пользователем
     * @throws UserAlreadyExistsException если имя пользователя уже занято
     */
    @Override
    public Mono<User> register(RegisterRequestDto registerRequestDto) {
        String username = registerRequestDto.username();
        String email = registerRequestDto.email();
        String password = registerRequestDto.password();
        log.debug("Registering user: {}", registerRequestDto.username());
        return webClientService.isEmailUnique(email)
                .flatMap(isUnique -> {
                    if (!isUnique) {
                        log.warn("Registration failed: email already exists: {}", email);
                        return Mono.error(new EmailNotUniqueException("Email already exists"));
                    }
                    return userRepository.existsByUsername(username);
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        log.warn("Registration failed: username already exists: {}", username);
                        return Mono.error(new UserAlreadyExistsException("Username already exists"));
                    }
                    User newUser = new User(username, passwordEncoder.encode(password));
                    return userRepository.save(newUser);
                })
                .flatMap(user -> attemptCreateProfileAndHandleRollback(user, email))
                .flatMap(user -> {
                    if (!user.getIsProfileCreated()) {
                        log.error("User {} was saved but profile was not created. " +
                                  "This should not happen if rollback worked correctly.", username);
                        return Mono.error(new RuntimeException("User saved but profile creation failed. " +
                                                               "Registration is inconsistent."));
                    }
                    log.debug("Assigning default role 'USER' to user: {}", username);
                    return findRoleIdByName("USER")
                            .flatMap(roleId -> userRoleRepository.insertUserRole(user.getId(), roleId)
                                    .thenReturn(user));
                })
                .doOnSuccess(savedUser -> log.info("User registered and profile created successfully: {}",
                        savedUser.getUsername()))
                .doOnError(error -> log.error("Registration failed for user: {}", username, error));
    }

    /**
     * Получает пользователя по его имени пользователя.
     * Не включает роли в возвращаемом объекте.
     *
     * @param username имя пользователя
     * @return реактивный объект с найденным пользователем
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<User> getUserByUsername(String username) {
        log.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)));
    }

    /**
     * Получает профиль текущего пользователя (включая роли).
     *
     * @param username имя текущего пользователя
     * @return реактивный объект с DTO профиля пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<UserProfileDto> getCurrentUserProfile(String username) {
        log.debug("Fetching profile for current user: {}", username);
        return userRepository.findByUsernameWithRoles(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)))
                .map(user -> new UserProfileDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEnabled(),
                        user.getRoles(),
                        user.getCreatedAt(),
                        user.getUpdatedAt()
                ))
                .doOnSuccess(_ -> log.debug("Profile fetched for user: {}", username))
                .doOnError(error -> log.error("Failed to fetch profile for user: {}", username, error));
    }

    /**
     * Получает профиль пользователя по его имени пользователя (включая роли).
     * Может использоваться администратором или для просмотра профиля другого пользователя.
     *
     * @param username имя пользователя, чей профиль нужно получить
     * @return реактивный объект с DTO профиля пользователя
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<UserProfileDto> getUserProfileByUsername(String username) {
        log.debug("Fetching profile for user by username: {}", username);
        return userRepository.findByUsernameWithRoles(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)))
                .map(userWithRoles -> new UserProfileDto(
                        userWithRoles.getId(),
                        userWithRoles.getUsername(),
                        userWithRoles.getEnabled(),
                        userWithRoles.getRoles(),
                        userWithRoles.getCreatedAt(),
                        userWithRoles.getUpdatedAt()
                ))
                .doOnSuccess(_ -> log.debug("User profile fetched successfully for: {}", username))
                .doOnError(error -> log.error("Failed to fetch user profile for: {}", username, error));
    }

    /**
     * Обновляет статус включения/отключения учетной записи пользователя.
     * При status = disabled, пользователь не может войти в систему.
     *
     * @param username имя пользователя
     * @param enabled  новый статус включения
     * @return реактивный объект с обновленным пользователем
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<User> updateUserEnabledStatus(String username, boolean enabled) {
        log.info("Updating user '{}' enabled status to: {}", username, enabled);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)))
                .flatMap(user -> {
                    user.setEnabled(enabled);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                })
                .doOnSuccess(_ -> log.info("User '{}' enabled status updated to: {}", username, enabled))
                .doOnError(error -> log.error("Failed to update enabled status for user: {}", username, error));
    }

    /**
     * Удаляет пользователя по его имени пользователя.
     * Также удаляет все связанные с ним refresh-токены.
     *
     * @param username имя пользователя для удаления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<Void> deleteUser(String username) {
        log.info("Deleting user: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found with username: " + username)))
                .flatMap(user -> userRefreshTokenRepository.deleteByUserId(user.getId())
                        .then(userRepository.deleteById(user.getId())))
                .doOnSuccess(_ -> log.info("User deleted successfully: {}", username))
                .doOnError(error -> log.error("Failed to delete user: {}", username, error));
        // TODO: Saga! Добавить удаление профиля пользователя в client-service
    }

    /**
     * Добавляет роль пользователю.
     * Если пользователь уже имеет эту роль, операция игнорируется.
     *
     * @param username имя пользователя
     * @param roleName название роли для добавления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     * @throws RoleNotFoundException если роль не найдена
     */
    @Override
    public Mono<Void> addRoleToUser(String username, String roleName) {
        log.info("Adding role '{}' to user '{}'", roleName, username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username)))
                .flatMap(user -> userRepository.findRoleIdByName(roleName)
                        .switchIfEmpty(Mono.error(new RoleNotFoundException("Role not found: " + roleName)))
                        .flatMap(roleId -> userRepository.hasRole(user.getId(), roleName)
                                .flatMap(hasRole -> {
                                    if (hasRole) {
                                        log.warn("User '{}' already has role '{}'", username, roleName);
                                        return Mono.empty();
                                    } else {
                                        log.debug("Adding role '{}' to user '{}'", roleName, username);
                                        return userRepository.addRoleToUser(user.getId(), roleId);
                                    }
                                })))
                .doOnSuccess(_ -> log.info("Role '{}' successfully added to user '{}'", roleName, username))
                .doOnError(error ->
                        log.error("Failed to add role '{}' to user '{}'", roleName, username, error));
        // TODO: Saga! Добавить добавление роли в профиль пользователя в client-service
    }

    /**
     * Удаляет роль у пользователя.
     * Предотвращает удаление последней роли пользователя.
     *
     * @param username имя пользователя
     * @param roleName название роли для удаления
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException        если пользователь не найден
     * @throws RoleNotFoundException        если роль не найдена
     * @throws LastRoleRemovalException     если попытка удалить последнюю роль пользователя
     */
    @Override
    public Mono<Void> removeRoleFromUser(String username, String roleName) {
        log.info("Removing role '{}' from user '{}'", roleName, username);
        return userRepository.findByUsernameWithRoles(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username)))
                .flatMap(dto -> {
                    List<String> roles = dto.getRoles();
                    if (roles == null || roles.isEmpty()) {
                        return Mono.error(new IllegalStateException("User has no roles"));
                    }
                    if (roles.size() == 1 && roles.contains(roleName)) {
                        return Mono.error(new LastRoleRemovalException("Cannot remove the last role of the user"));
                    }
                    return userRepository.findRoleIdByName(roleName)
                            .switchIfEmpty(Mono.error(new RoleNotFoundException("Role not found: " + roleName)))
                            .flatMap(roleId -> userRepository.removeRoleFromUser(dto.getId(), roleId));
                })
                .doOnSuccess(_ -> log.info("Role '{}' successfully removed from user '{}'", roleName, username))
                .doOnError(error ->
                        log.error("Failed to remove role '{}' from user '{}'", roleName, username, error));
        // TODO: Saga! Добавить удаление роли из профиля пользователя в client-service
    }

    /**
     * Изменяет пароль текущего пользователя.
     * Проверяет, совпадает ли старый пароль, и не совпадает ли новый пароль со старым.
     *
     * @param username                    имя текущего пользователя
     * @param changePasswordRequestDto DTO с текущим и новым паролем
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException       если пользователь не найден
     * @throws InvalidCredentialsException если старый пароль неверен
     */
    @Override
    public Mono<Void> changeCurrentUserPassword(String username, ChangePasswordRequestDto changePasswordRequestDto) {
        log.info("Change password request for user: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + username)))
                .filter(user -> passwordEncoder.matches(changePasswordRequestDto.oldPassword(), user.getPassword()))
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Old password is incorrect")))
                .filter(_ -> !changePasswordRequestDto.oldPassword().equals(changePasswordRequestDto.newPassword()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("New password cannot be the same as the " +
                                                                       "old password")))
                .flatMap(user -> {
                    String encodedNewPassword = passwordEncoder.encode(changePasswordRequestDto.newPassword());
                    user.setPassword(encodedNewPassword);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                })
                .then()
                .doOnSuccess(_ -> log.info("Password changed successfully for user: {}", username))
                .doOnError(error -> log.error("Failed to change password for user: {}", username, error));
    }

    /**
     * Изменяет пароль пользователя по инициативе администратора.
     * Инвалидирует все refresh-токены пользователя после смены пароля.
     *
     * @param targetUsername                  имя пользователя, пароль которого нужно изменить
     * @param adminChangePasswordRequestDto DTO с новым паролем
     * @return реактивный объект, сигнализирующий о завершении операции
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Mono<Void> changeUserPasswordByAdmin(String targetUsername,
                                                AdminChangePasswordRequestDto adminChangePasswordRequestDto) {
        log.info("Admin initiated password change request for user: {}", targetUsername);

        return userRepository.findByUsername(targetUsername)
                .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + targetUsername)))
                .flatMap(user -> {
                    log.debug("Invalidating all refresh tokens for user: {}", targetUsername);
                    return userRefreshTokenRepository.deleteByUserId(user.getId())
                            .then(Mono.just(user));
                })
                .flatMap(user -> {
                    String encodedNewPassword = passwordEncoder.encode(adminChangePasswordRequestDto.newPassword());
                    user.setPassword(encodedNewPassword);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                })
                .then()
                .doOnSuccess(_ -> log.info("Password successfully changed for user: {} by an admin.",
                        targetUsername))
                .doOnError(error -> log.error("Failed to change password for user: {} by an admin.",
                        targetUsername, error));
    }

    /**
     * Пытается создать профиль в client-service.
     * Если не удаётся, откатывает регистрацию пользователя в auth-service.
     *
     * @param user     созданный пользователь в auth-service
     * @param email    email пользователя для создания профиля
     * @return реактивный объект с пользователем, у которого обновлен флаг isProfileCreated
     */
    private Mono<User> attemptCreateProfileAndHandleRollback(User user, String email) {
        String username = user.getUsername();
        log.info("User saved in auth-service: {}. Attempting to create profile in client-service.", username);
        return webClientService.createProfile(username, email)
                .doOnSuccess(_ -> {
                    log.info("Profile created successfully in client-service for user: {}", username);
                    user.setIsProfileCreated(true);
                    user.setUpdatedAt(Instant.now());
                })
                .then(userRepository.save(user))
                .onErrorResume(error -> {
                    log.error("Failed to create profile in client-service for user: {}. " +
                              "Rolling back user registration. Error: {}", username, error.getMessage(), error);
                    return rollbackUserRegistration(user.getId(), username)
                            .then(Mono.error(error));
                });
    }

    /**
     * Откатывает регистрацию пользователя в auth-service.
     * Удаляет пользователя и связанные с ним роли и refresh-токены. Saga registration rollBack.
     *
     * @param userId   идентификатор пользователя для удаления
     * @param username имя пользователя для логирования
     * @return реактивный объект, сигнализирующий о завершении операции отката
     */
    private Mono<Void> rollbackUserRegistration(UUID userId, String username) {
        log.info("Starting rollback for user: {}", username);
        return userRepository.deleteById(userId)
                .then(userRoleRepository.deleteById(userId))
                .then(userRefreshTokenRepository.deleteByUserId(userId))
                .doOnSuccess(_ -> log.info("Rollback completed successfully for user: {}", username))
                .doOnError(error -> log.error("Rollback failed for user: {}", username, error))
                .onErrorMap(error -> new RuntimeException("Rollback failed for user " + username + ": "
                                                          + error.getMessage(), error));
    }

    /**
     * Генерирует случайный UUID для использования в качестве refresh-токена.
     *
     * @return строка с новым refresh-токеном
     */
    private String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Находит идентификатор роли по её названию.
     * Используется, например, для присвоения роли 'USER' новому пользователю.
     *
     * @param roleName название роли
     * @return реактивный объект с идентификатором роли
     * @throws RuntimeException если роль не найдена в базе данных
     */
    private Mono<UUID> findRoleIdByName(String roleName) {
        return roleRepository.findByName(roleName)
                .switchIfEmpty(Mono.error(new RuntimeException("Default role 'USER' not found in database.")))
                .map(Role::getId);
    }

    /**
     * Вспомогательная запись для передачи данных между этапами обновления токена.
     *
     * @param tokenEntity сущность refresh-токена
     * @param user        пользователь, связанный с токеном
     */
    private record RefreshTokenValidationResult(UserRefreshToken tokenEntity, User user) {
    }
}

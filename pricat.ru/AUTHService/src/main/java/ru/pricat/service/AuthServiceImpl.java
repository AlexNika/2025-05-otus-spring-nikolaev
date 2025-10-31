package ru.pricat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.pricat.exception.InvalidCredentialsException;
import ru.pricat.exception.UserAlreadyExistsException;
import ru.pricat.model.User;
import ru.pricat.model.dto.LoginResponse;
import ru.pricat.repository.UserRepository;
import ru.pricat.util.JwtUtil;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final LoginAttemptService loginAttemptService;

    @Override
    public Mono<LoginResponse> authenticate(String username, String password) {
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
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        loginAttemptService.loginFailed(username);
                        log.warn("Invalid password for user: {}", username);
                    }
                    return matches;
                })
                .switchIfEmpty(Mono.error(new InvalidCredentialsException("Invalid credentials")))
                .doOnNext(_ -> loginAttemptService.loginSucceeded(username))
                .map(user -> {
                    log.info("User authenticated successfully: {}", username);
                    String token = jwtUtil.generateToken(user.getUsername(), user.getRoles());
                    return new LoginResponse(token, TOKEN_TYPE, jwtUtil.getExpiration());
                });
    }

    @Override
    public Mono<User> register(User user) {
        log.debug("Registering user: {}", user.getUsername());
        return Mono.zip(
                        userRepository.existsByUsername(user.getUsername()),
                        userRepository.existsByEmail(user.getEmail())
                )
                .flatMap(tuple -> {
                    boolean usernameExists = tuple.getT1();
                    boolean emailExists = tuple.getT2();

                    if (usernameExists) {
                        log.warn("Registration failed: username already exists: {}", user.getUsername());
                        return Mono.error(new UserAlreadyExistsException("Username already exists"));
                    }
                    if (emailExists) {
                        log.warn("Registration failed: email already exists: {}", user.getEmail());
                        return Mono.error(new UserAlreadyExistsException("Email already exists"));
                    }

                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    log.debug("Saving new user: {}", user.getUsername());
                    return userRepository.save(user);
                })
                .doOnSuccess(savedUser -> log.info("User registered successfully: {}", savedUser.getUsername()));
    }
}

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.pricat.exception.ErrorResponse;
import ru.pricat.exception.InvalidCredentialsException;
import ru.pricat.exception.UserAlreadyExistsException;
import ru.pricat.model.User;
import ru.pricat.model.dto.LoginRequest;
import ru.pricat.model.dto.LoginResponse;
import ru.pricat.service.AuthService;
import ru.pricat.service.TokenBlacklistService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller", description = "APIs for user authentication, registration, logout and refresh")
public class AuthController {

    private final AuthService authService;

    private final TokenBlacklistService tokenBlacklistService;

    private final ReactiveJwtDecoder jwtDecoder;

    @Operation(summary = "Authenticate user", description = "Logs in a user and returns a JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    @PostMapping("/login")
    public Mono<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class))
            )
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.username());
        return authService.authenticate(loginRequest.username(), loginRequest.password())
                .doOnSuccess(_ -> log.info("Login successful for user: {}", loginRequest.username()))
                .doOnError(error -> log.error("Login failed for user: {}", loginRequest.username(), error))
                .onErrorResume(InvalidCredentialsException.class, ex -> {
                    throw ex;
                });
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account.")
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
                    content = @Content(schema = @Schema(implementation = User.class))
            )
            @Valid @RequestBody User user) {
        log.info("Registration attempt for user: {}", user.getUsername());
        return authService.register(user)
                .doOnSuccess(savedUser -> log.info("User registered successfully: {}", savedUser.getUsername()))
                .doOnError(error -> log.error("Registration failed for user: {}", user.getUsername(), error))
                .onErrorResume(UserAlreadyExistsException.class, ex -> {
                    throw ex;
                });
    }

    @Operation(summary = "Logout user", description = "Invalidates the current JWT token by adding it to the " +
                                                      "blacklist.")
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

    @Operation(summary = "Get user info", description = "Returns a simple message accessible to users with 'USER' " +
                                                        "or 'ADMIN' roles.")
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

    @Operation(summary = "Get current user info", description = "Returns a simple message accessible to any " +
                                                                "authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user info retrieved",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<String> getCurrentUser() {
        log.debug("Accessing current user endpoint");
        return Mono.just("Current user endpoint");
    }
}

package ru.pricat.service;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.pricat.config.properties.AuthServiceUrlConfig;
import ru.pricat.exception.AuthServiceException;
import ru.pricat.model.dto.auth.LoginRequestDto;
import ru.pricat.model.dto.auth.LoginResponseDto;
import ru.pricat.model.dto.auth.RegisterRequestDto;
import ru.pricat.model.dto.auth.UserResponseDto;
import ru.pricat.model.dto.request.AddRoleRequestDto;
import ru.pricat.model.dto.request.AdminChangePasswordRequestDto;
import ru.pricat.model.dto.request.ChangePasswordRequestDto;
import ru.pricat.model.dto.request.RemoveRoleRequestDto;

import java.util.function.Supplier;

import static ru.pricat.util.AppConstants.API_V1_AUTH_PATH;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final RestClient.Builder restClientBuilder;

    private final AuthServiceUrlConfig authServiceUrlConfig;

    private final CircuitBreaker circuitBreaker;

    private final Retry retry;

    private final RateLimiter rateLimiter;

    public AuthServiceImpl(RestClient.Builder restClientBuilder,
                           AuthServiceUrlConfig authServiceUrlConfig,
                           CircuitBreakerRegistry circuitBreakerRegistry,
                           RetryRegistry retryRegistry,
                           RateLimiterRegistry rateLimiterRegistry) {
        this.restClientBuilder = restClientBuilder;
        this.authServiceUrlConfig = authServiceUrlConfig;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("auth-service");
        this.retry = retryRegistry.retry("auth-service");
        this.rateLimiter = rateLimiterRegistry.rateLimiter("auth-service");
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request) {
        return executeWithResilience(() -> doLogin(request));
    }

    private LoginResponseDto doLogin(@NonNull LoginRequestDto request) {
        log.info("Calling auth-service /login for user: {}", request.username());
        String url = getAuthServiceUrl() + "/login";
        log.debug("auth-service login uri: {}", url);
        try {
            LoginResponseDto response = restClientBuilder.build()
                    .post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .body(LoginResponseDto.class);
            log.info("Successfully logged in user: {}", request.username());
            return response;
        } catch (RestClientResponseException e) {
            log.error("Error logging in user: {}. Status: {}, Response: {}", request.username(), e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Login failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error logging in user: {}", request.username(), e);
            throw new AuthServiceException("Login service unavailable", e);
        }
    }

    @Override
    public void logout(String jwtToken) {
        executeWithResilience(() -> doLogout(jwtToken));
    }

    private void doLogout(String jwtToken) {
        log.info("Calling auth-service /logout");
        String url = getAuthServiceUrl() + "/logout";
        log.debug("auth-service logout uri: {}", url);
        try {
            restClientBuilder.build()
                    .post()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully logged out");
        } catch (RestClientResponseException e) {
            log.error("Error logging out. Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthServiceException("Logout failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error logging out", e);
            throw new AuthServiceException("Logout service unavailable", e);
        }
    }

    @Override
    public void register(RegisterRequestDto request) {
        executeWithResilience(() -> doRegister(request));
    }

    private void doRegister(@NonNull RegisterRequestDto request) {
        log.info("Calling auth-service /register for user: {}", request.username());
        String url = getAuthServiceUrl() + "/register";
        log.debug("auth-service register uri: {}", url);
        try {
            restClientBuilder.build()
                    .post()
                    .uri(url)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully registered user: {}", request.username());
        } catch (RestClientResponseException e) {
            log.error("Error registering user: {}. Status: {}, Response: {}", request.username(), e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Registration failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error registering user: {}", request.username(), e);
            throw new AuthServiceException("Registration service unavailable", e);
        }
    }

    @Override
    public UserResponseDto getMe(String jwtToken) {
        return executeWithResilience(() -> doGetMe(jwtToken));
    }

    private UserResponseDto doGetMe(String jwtToken) {
        log.info("Calling auth-service /me");
        String url = getAuthServiceUrl() + "/me";
        log.debug("auth-service me uri: {}", url);
        try {
            UserResponseDto response = restClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(UserResponseDto.class);
            log.info("Successfully retrieved me user info: {}", response != null ? response.username() : null);
            return response;
        } catch (RestClientResponseException e) {
            log.error("Error retrieving user info. Status: {}, Response: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Get user info failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving user info", e);
            throw new AuthServiceException("Get user info service unavailable", e);
        }
    }

    @Override
    public UserResponseDto getUser(String username, String jwtToken) {
        return executeWithResilience(() -> doGetUser(username, jwtToken));
    }

    private UserResponseDto doGetUser(String username, String jwtToken) {
        log.info("Calling auth-service /user/{}", username);
        String url = getAuthServiceUrl() + "/user/" + username;
        log.debug("auth-service user uri: {}", url);
        try {
            UserResponseDto response = restClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .body(UserResponseDto.class);
            log.info("Successfully retrieved user info: {}", response != null ? response.username() : null);
            return response;
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found: {}", username);
                throw new AuthServiceException("User not found: " + username, e);
            }
            log.error("Error retrieving user info for: {}. Status: {}, Response: {}", username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Get user failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving user info for: {}", username, e);
            throw new AuthServiceException("Get user service unavailable", e);
        }
    }

    @Override
    public LoginResponseDto refresh(String refreshToken) {
        return executeWithResilience(() -> doRefresh(refreshToken));
    }

    private LoginResponseDto doRefresh(String refreshToken) {
        log.info("Calling auth-service /refresh");
        String url = getAuthServiceUrl() + "/refresh";
        log.debug("auth-service refresh uri: {}", url);
        try {
            LoginResponseDto response = restClientBuilder.build()
                    .post()
                    .uri(url)
                    .header("Cookie", "refresh-token=" + refreshToken)
                    .retrieve()
                    .body(LoginResponseDto.class);
            log.info("Successfully refreshed token");
            return response;
        } catch (RestClientResponseException e) {
            log.error("Error refreshing token. Status: {}, Response: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Token refresh failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error refreshing token", e);
            throw new AuthServiceException("Token refresh service unavailable", e);
        }
    }

    @Override
    public void deleteUser(String username, String jwtToken) {
        executeWithResilience(() -> doDeleteUser(username, jwtToken));
    }

    private void doDeleteUser(String username, String jwtToken) {
        log.info("Calling auth-service to delete user: {}", username);
        String url = getAuthServiceUrl() + "/user/" + username;
        log.debug("auth-service delete user uri: {}", url);
        try {
            restClientBuilder.build()
                    .delete()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully deleted user: {}", username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found for deletion: {}", username);
                throw new AuthServiceException("User not found for deletion: " + username, e);
            }
            log.error("Error deleting user: {}. Status: {}, Response: {}", username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Delete user failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error deleting user: {}", username, e);
            throw new AuthServiceException("Delete user service unavailable", e);
        }
    }

    @Override
    public void enableUser(String username, String jwtToken) {
        executeWithResilience(() -> doEnableUser(username, jwtToken));
    }

    private void doEnableUser(String username, String jwtToken) {
        log.info("Calling auth-service to enable user: {}", username);
        String url = getAuthServiceUrl() + "/user/" + username + "/enable";
        log.debug("auth-service enable user uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully enabled user: {}", username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found for enabling: {}", username);
                throw new AuthServiceException("User not found for enabling: " + username, e);
            }
            log.error("Error enabling user: {}. Status: {}, Response: {}", username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Enable user failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error enabling user: {}", username, e);
            throw new AuthServiceException("Enable user service unavailable", e);
        }
    }

    @Override
    public void disableUser(String username, String jwtToken) {
        executeWithResilience(() -> doDisableUser(username, jwtToken));
    }

    private void doDisableUser(String username, String jwtToken) {
        log.info("Calling auth-service to disable user: {}", username);
        String url = getAuthServiceUrl() + "/user/" + username + "/disable";
        log.debug("auth-service disable user uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully disabled user: {}", username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found for disabling: {}", username);
                throw new AuthServiceException("User not found for disabling: " + username, e);
            }
            log.error("Error disabling user: {}. Status: {}, Response: {}", username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Disable user failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error disabling user: {}", username, e);
            throw new AuthServiceException("Disable user service unavailable", e);
        }
    }

    @Override
    public void addRole(String username, String role, String jwtToken) {
        executeWithResilience(() -> doAddRole(username, role, jwtToken));
    }

    private void doAddRole(String username, String role, String jwtToken) {
        log.info("Calling auth-service to add role '{}' to user: {}", role, username);
        String url = getAuthServiceUrl() + "/user/" + username + "/add-role";
        log.debug("auth-service add role uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(new AddRoleRequestDto(role))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully added role '{}' to user: {}", role, username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User or role not found when adding role: user={}, role={}", username, role);
                throw new AuthServiceException("User or role not found: " + e.getResponseBodyAsString(), e);
            }
            log.error("Error adding role '{}' to user: {}. Status: {}, Response: {}", role, username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Add role failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error adding role '{}' to user: {}", role, username, e);
            throw new AuthServiceException("Add role service unavailable", e);
        }
    }

    @Override
    public void removeRole(String username, String role, String jwtToken) {
        executeWithResilience(() -> doRemoveRole(username, role, jwtToken));
    }

    private void doRemoveRole(String username, String role, String jwtToken) {
        log.info("Calling auth-service to remove role '{}' from user: {}", role, username);
        String url = getAuthServiceUrl() + "/user/" + username + "/remove-role";
        log.debug("auth-service remove role uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(new RemoveRoleRequestDto(role))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully removed role '{}' from user: {}", role, username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User or role not found when removing role: user={}, role={}", username, role);
                throw new AuthServiceException("User or role not found: " + e.getResponseBodyAsString(), e);
            }
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.warn("Cannot remove the last role of the user: user={}, role={}", username, role);
                throw new AuthServiceException("Cannot remove the last role of the user", e);
            }
            log.error("Error removing role '{}' from user: {}. Status: {}, Response: {}", role, username,
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new AuthServiceException("Remove role failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error removing role '{}' from user: {}", role, username, e);
            throw new AuthServiceException("Remove role service unavailable", e);
        }
    }

    @Override
    public void changePassword(ChangePasswordRequestDto changePasswordRequestDto, String jwtToken) {
        executeWithResilience(() -> doChangePassword(changePasswordRequestDto, jwtToken));
    }

    private void doChangePassword(ChangePasswordRequestDto changePasswordRequestDto, String jwtToken) {
        log.info("Calling auth-service to change current user's password");
        String url = getAuthServiceUrl() + "/me/change-password";
        log.debug("auth-service change password uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(changePasswordRequestDto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully changed current user's password");
        } catch (RestClientResponseException e) {
            log.error("Error changing current user's password. Status: {}, Response: {}", e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Change password failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error changing current user's password", e);
            throw new AuthServiceException("Change password service unavailable", e);
        }
    }

    @Override
    public void changeUserPasswordByAdmin(String username, String newPassword, String jwtToken) {
        executeWithResilience(() -> doChangeUserPasswordByAdmin(username, newPassword, jwtToken));
    }

    private void doChangeUserPasswordByAdmin(String username, String newPassword, String jwtToken) {
        log.info("Calling auth-service to change password for user: {} by admin", username);
        String url = getAuthServiceUrl() + "/user/" + username + "/change-password";
        log.debug("auth-service admin change password uri: {}", url);
        try {
            restClientBuilder.build()
                    .patch()
                    .uri(url)
                    .header("Authorization", "Bearer " + jwtToken)
                    .body(new AdminChangePasswordRequestDto(newPassword))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Successfully changed password for user: {} by admin", username);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("User not found when changing password: {}", username);
                throw new AuthServiceException("User not found: " + username, e);
            }
            log.error("Error changing password for user: {}. Status: {}, Response: {}", username, e.getStatusCode(),
                    e.getResponseBodyAsString());
            throw new AuthServiceException("Admin change password failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error changing password for user: {}", username, e);
            throw new AuthServiceException("Admin change password service unavailable", e);
        }
    }

    @NonNull
    private String getAuthServiceUrl() {
        return authServiceUrlConfig.getAuthServiceLbUrl() + API_V1_AUTH_PATH;
    }

    private void executeWithResilience(Runnable method) {
        Supplier<Void> supplier = () -> {
            method.run();
            return null;
        };
        Supplier<Void> decoratedSupplier = decorateWithRateLimiter(
                decorateWithCircuitBreaker(
                        decorateWithRetry(supplier)
                )
        );
        decoratedSupplier.get();
    }

    private <T> T executeWithResilience(Supplier<T> method) {
        Supplier<T> decoratedSupplier = decorateWithRateLimiter(
                decorateWithCircuitBreaker(
                        decorateWithRetry(method)
                )
        );
        return decoratedSupplier.get();
    }

    @NonNull
    private <T> Supplier<T> decorateWithRetry(Supplier<T> supplier) {
        return Retry
                .decorateSupplier(
                        retry,
                        () -> {
                            log.debug("Executing method with retry logic");
                            return supplier.get();
                        }
                );
    }

    @NonNull
    private <T> Supplier<T> decorateWithCircuitBreaker(Supplier<T> supplier) {
        return CircuitBreaker
                .decorateSupplier(
                        circuitBreaker,
                        () -> {
                            log.debug("Executing method with circuit breaker logic");
                            return supplier.get();
                        }
                );
    }

    @NonNull
    private <T> Supplier<T> decorateWithRateLimiter(Supplier<T> supplier) {
        return RateLimiter
                .decorateSupplier(
                        rateLimiter,
                        () -> {
                            log.debug("Executing method with rate limiter logic");
                            return supplier.get();
                        }
                );
    }
}

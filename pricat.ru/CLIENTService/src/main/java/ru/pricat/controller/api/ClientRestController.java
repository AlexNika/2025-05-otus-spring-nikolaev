package ru.pricat.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.pricat.exception.ServiceUnavailableException;
import ru.pricat.model.dto.request.ProfilePatchRequestDto;
import ru.pricat.model.dto.request.ProfileUpdateRequestDto;
import ru.pricat.model.dto.response.AdminProfileDto;
import ru.pricat.model.dto.response.ProfileResponseDto;
import ru.pricat.service.ClientService;

import jakarta.validation.Valid;

import static ru.pricat.util.AppConstants.API_V1_CLIENT_PATH;

/**
 * REST API контроллер для управления клиентскими профилями.
 * Предоставляет REST endpoints для получения, обновления и удаления профилей пользователей.
 * Используется для AJAX запросов из web интерфейса.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(API_V1_CLIENT_PATH)
@Validated
@Tag(name = "Profile Management API", description = "REST API for managing user profiles")
public class ClientRestController {

    private final ClientService clientService;

    /**
     * Получает профиль текущего аутентифицированного пользователя.
     *
     * @param auth объект аутентификации Spring Security
     * @return профиль пользователя
     */
    @Operation(summary = "Get current user profile",
            description = "Returns the profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/me")
    public ResponseEntity<AdminProfileDto> getCurrentUserProfile(Authentication auth) {
        String username = auth.getName();
        log.info("Getting current user profile for: {}", username);

        AdminProfileDto profile = clientService.getCurrentUserProfile(username);
        log.info("Successfully retrieved profile for: {}", username);

        return ResponseEntity.ok(profile);
    }

    /**
     * Полностью обновляет профиль текущего пользователя.
     * Проверяет уникальность email при изменении.
     *
     * @param auth объект аутентификации
     * @param request DTO с данными для обновления
     * @return обновленный профиль пользователя
     */
    @Operation(summary = "Update current user profile",
            description = "Updates all fields of the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/me")
    public ResponseEntity<ProfileResponseDto> updateCurrentUserProfile(
            Authentication auth,
            @Valid @RequestBody ProfileUpdateRequestDto request) {

        String username = auth.getName();
        log.info("Updating all profile fields for user: {}", username);

        ProfileResponseDto updatedProfile = clientService.updateCurrentUserProfile(username, request);
        log.info("Successfully updated profile for: {}", username);

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Частично обновляет профиль текущего пользователя.
     * Обновляет только переданные поля.
     *
     * @param auth объект аутентификации
     * @param request DTO с частичными данными для обновления
     * @return обновленный профиль пользователя
     */
    @Operation(summary = "Partially update current user profile",
            description = "Updates one or more fields of the authenticated user's profile")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PatchMapping("/me")
    public ResponseEntity<ProfileResponseDto> patchCurrentUserProfile(
            Authentication auth,
            @Valid @RequestBody ProfilePatchRequestDto request) {

        String username = auth.getName();
        log.info("Partially updating profile for user: {}", username);

        ProfileResponseDto updatedProfile = clientService.patchCurrentUserProfile(username, request);
        log.info("Successfully updated partial profile for: {}", username);

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Получает профиль пользователя по username.
     * Доступно только для пользователей с ролью ADMIN.
     *
     * @param username имя пользователя для поиска
     * @return профиль запрашиваемого пользователя
     */
    @Operation(summary = "Get user profile by username",
            description = "Returns the profile of a user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public ResponseEntity<AdminProfileDto> getUserProfile(@PathVariable String username) {
        log.info("Admin getting profile for user: {}", username);

        AdminProfileDto profile = clientService.getUserProfile(username);
        log.info("Admin successfully retrieved profile for: {}", username);

        return ResponseEntity.ok(profile);
    }

    /**
     * Обновляет профиль пользователя по username.
     * Доступно только для администраторов.
     *
     * @param username имя пользователя для обновления
     * @param request DTO с данными для обновления
     * @return обновленный профиль пользователя
     */
    @Operation(summary = "Update user profile by username",
            description = "Updates the profile of a user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{username}")
    public ResponseEntity<ProfileResponseDto> updateUserProfile(
            @PathVariable String username,
            @Valid @RequestBody ProfileUpdateRequestDto request) {

        log.info("Admin updating profile for user: {}", username);

        ProfileResponseDto updatedProfile = clientService.updateUserProfile(username, request);
        log.info("Admin successfully updated profile for: {}", username);

        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * Удаляет профиль пользователя по username.
     * Доступно только для администраторов.
     *
     * @param username имя пользователя для удаления
     * @return HTTP 204 No Content при успешном удалении
     */
    @Operation(summary = "Delete user profile",
            description = "Deletes the profile of a user (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Profile deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteProfile(@PathVariable String username) {
        log.info("Admin deleting profile for user: {}", username);

        clientService.deleteProfile(username);
        log.info("Successfully deleted profile for: {}", username);

        return ResponseEntity.noContent().build();
    }

    /**
     * Получает список всех клиентов с пагинацией.
     * Доступно только для пользователей с ролью ADMIN.
     * Возвращает страницу с клиентами, отсортированными по username в алфавитном порядке.
     *
     * @param page номер страницы (начинается с 0, по умолчанию 0)
     * @param size количество элементов на странице (по умолчанию 10)
     * @return страница с DTO профилей клиентов
     * @throws ServiceUnavailableException если сервис временно недоступен
     */
    @Operation(summary = "Get all clients with pagination",
            description = "Returns paginated list of all clients (admin only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clients retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "503", description = "Service unavailable")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Page<AdminProfileDto>> getAllClients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Getting all clients - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<AdminProfileDto> clientsPage = clientService.getAllClients(pageable);

        return ResponseEntity.ok(clientsPage);
    }
}

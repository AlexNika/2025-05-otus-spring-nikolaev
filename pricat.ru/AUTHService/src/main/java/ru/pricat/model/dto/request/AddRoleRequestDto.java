package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для запроса на добавление роли пользователю.
 * В тексте запроса ожидается имя роли.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AddRoleRequestDto(
        @NotBlank(message = "Role name is required")
        @Pattern(regexp = "^[A-Z_]+$",
                message = "Role name must be in uppercase and underscore format (e.g., 'USER', 'ADMIN')")
        String role
) {}

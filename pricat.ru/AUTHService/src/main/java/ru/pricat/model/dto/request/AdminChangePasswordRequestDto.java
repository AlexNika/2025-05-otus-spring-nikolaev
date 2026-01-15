package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для запроса администратора изменить пароль пользователя.
 * Ожидает, что новый пароль будет указан в теле запроса.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AdminChangePasswordRequestDto(
        @NotBlank(message = "New password is required")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit, and one special character."
        )
        String newPassword
) {}
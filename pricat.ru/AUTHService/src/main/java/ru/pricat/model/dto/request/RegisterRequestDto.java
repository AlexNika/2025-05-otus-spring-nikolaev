package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса на регистрацию пользователя.
 * Содержит только имя пользователя, email и пароль.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterRequestDto(
        @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
        @NotBlank(message = "Username is required")
        String username,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit, and one special character."
        )
        @NotBlank(message = "Password is required")
        String password) {
}

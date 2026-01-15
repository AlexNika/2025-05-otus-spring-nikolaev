package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO для запроса на изменение пароля пользователя, прошедшего проверку подлинности в данный момент.
 * В теле запроса ожидаются старый и новый пароли.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ChangePasswordRequestDto(
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit, and one special character."
        )
        @NotBlank(message = "New password is required")
        String newPassword
) {}

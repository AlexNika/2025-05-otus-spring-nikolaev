package ru.pricat.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for requesting to change the password of the currently authenticated user.
 * Expects the old password and the new password in the request body.
 */
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

package ru.pricat.model.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record RegisterRequestDto(
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
        String username,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Username cannot be empty")
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit, and one special character."
        )
        @NotBlank(message = "Username cannot be empty")
        String password
) {}

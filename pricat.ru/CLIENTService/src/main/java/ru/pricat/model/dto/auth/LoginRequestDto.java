package ru.pricat.model.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record LoginRequestDto(
        @NotBlank(message = "Username cannot be empty")
        @Size(min = 3, max = 64, message = "Username must be between 3 and 64 characters")
        String username,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be at least 12 characters long and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit, and one special character."
        )
        @NotBlank(message = "Password cannot be empty")
        String password
) {}

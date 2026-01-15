package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for requesting to remove a role from a user.
 * Expects role name in the request body.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RemoveRoleRequestDto(
        @NotBlank(message = "Role name is required")
        @Pattern(regexp = "^[A-Z_]+$",
                message = "Role name must be in uppercase and underscore format (e.g., 'USER', 'ADMIN')")
        String role
) {}

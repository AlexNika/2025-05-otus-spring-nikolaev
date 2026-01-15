package ru.pricat.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for requesting to remove a role from a user.
 * Expects role name in the request body.
 */
public record RemoveRoleRequestDto(
        @NotBlank(message = "Role name is required")
        @Pattern(regexp = "^[A-Z_]+$",
                message = "Role name must be in uppercase and underscore format (e.g., 'USER', 'ADMIN')")
        String role
) {}

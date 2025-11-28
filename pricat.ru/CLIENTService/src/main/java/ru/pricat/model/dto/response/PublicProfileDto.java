package ru.pricat.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import java.util.UUID;
import java.time.OffsetDateTime;

/**
 * DTO for public user profile (visible to non-admin users).
 * Does not include supplier-specific fields.
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record PublicProfileDto(
        UUID id,
        @NotBlank(message = "Username is required") String username,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email,
        String name,
        @NotNull(message = "Role is required") String roles,
        @NotBlank(message = "Company name is required") String companyName,
        String mobilePhone,
        String avatarUrl,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}

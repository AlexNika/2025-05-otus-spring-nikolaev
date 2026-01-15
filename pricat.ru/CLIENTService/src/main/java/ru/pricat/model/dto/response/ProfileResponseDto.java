package ru.pricat.model.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.pricat.model.Client;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * DTO for {@link ru.pricat.model.Client}
 */
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileResponseDto(
        UUID id,
        @NotBlank(message = "Username is required") String username,
        @Email(message = "Email should be valid") @NotBlank(message = "Email is required") String email,
        String name,
        @NotNull(message = "Role is required") String roles,
        @NotBlank(message = "Company name is required") String companyName,
        String mobilePhone,
        String avatarUrl,
        @NotNull(message = "Is supplier flag is required") boolean isSupplier,
        String companyFolder,
        @NotNull(message = "Price list obtaining way is required") Client.PricelistObtainingWay pricelistObtainingWay,
        @NotNull(message = "Price list format is required") Client.PricelistFormat pricelistFormat,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {}

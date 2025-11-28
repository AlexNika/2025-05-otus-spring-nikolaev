package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.pricat.model.Client;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileUpdateRequestDto(
        String name,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Company name is required")
        String companyName,

        String mobilePhone,

        String avatarUrl,

        @NotNull(message = "Is supplier flag is required")
        boolean isSupplier,

        String companyFolder,

        @NotNull(message = "Pricelist obtaining way is required")
        Client.PricelistObtainingWay pricelistObtainingWay,

        @NotNull(message = "Pricelist format is required")
        Client.PricelistFormat pricelistFormat
) {}

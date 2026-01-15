package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import ru.pricat.model.Client;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfilePatchRequestDto(
        String name,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Company name is required")
        String companyName,
        String mobilePhone,

        Boolean isSupplier,
        String companyFolder,
        Client.PricelistObtainingWay pricelistObtainingWay,
        Client.PricelistFormat pricelistFormat
) {}

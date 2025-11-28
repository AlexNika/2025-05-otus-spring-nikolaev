package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record EmailCheckRequestDto(
        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email
) {}
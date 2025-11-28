package ru.pricat.model.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProfileCreateRequestDto(
        @NotBlank(message = "Username cannot be empty")
        String username,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Username cannot be empty")
        String email,

        @Size(max = 255, message = "Max name length should not exceed 255 characters")
        String name
) {}

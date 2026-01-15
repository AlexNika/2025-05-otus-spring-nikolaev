package ru.pricat.model.dto.auth;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record UserResponseDto(
        String username,
        String email,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {}

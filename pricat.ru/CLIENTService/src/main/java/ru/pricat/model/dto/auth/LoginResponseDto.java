package ru.pricat.model.dto.auth;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String username,
        String role
) {}

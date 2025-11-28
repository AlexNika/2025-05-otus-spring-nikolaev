package ru.pricat.model.dto.response;

public record LoginResponseDto(String accessToken, String refreshToken, String tokenType, Long expiresIn) {
}

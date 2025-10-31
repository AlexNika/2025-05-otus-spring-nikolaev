package ru.pricat.model.dto;

public record LoginResponse(String accessToken, String tokenType, Long expiresIn) {
}

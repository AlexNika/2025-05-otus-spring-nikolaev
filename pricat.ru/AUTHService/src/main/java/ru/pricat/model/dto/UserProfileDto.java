package ru.pricat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для представления основной информации профиля пользователя, полученной из auth-сервиса.
 * Предназначен для использования в endpoints /me, /user/{username}.
 * Не содержит конфиденциальной информации, такой как пароль или адрес электронной почты
 * (электронная почта управляется clients-service).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserProfileDto(
        UUID id,
        String username,
        Boolean enabled,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
    public UserProfileDto(UUID id, String username, Boolean enabled, List<String> roles, Instant createdAt,
                          Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.enabled = enabled;
        this.roles = roles != null ? roles : List.of();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

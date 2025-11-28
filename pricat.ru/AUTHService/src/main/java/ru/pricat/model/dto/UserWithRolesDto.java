package ru.pricat.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO для передачи данных о пользователе вместе с его ролями.
 * Используется, например, для возврата информации о пользователе с ролями из репозитория.
 * Аннотация @JsonIgnoreProperties(ignoreUnknown = true) позволяет игнорировать
 * неизвестные поля при десериализации из JSON, что повышает устойчивость к изменениям.
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserWithRolesDto {
    private UUID id;
    private String username;
    private String password;
    private Boolean enabled;
    private Boolean isProfileCreated;
    private List<String> roles = List.of();
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Конструктор с полным набором параметров.
     *
     * @param id              уникальный идентификатор пользователя
     * @param username        имя пользователя
     * @param password        хешированный пароль пользователя
     * @param enabled         статус включения учетной записи
     * @param isProfileCreated флаг, указывающий, создан ли профиль пользователя в client-service
     * @param roles           список ролей пользователя
     * @param createdAt       дата и время создания пользователя
     * @param updatedAt       дата и время последнего обновления пользователя
     */
    public UserWithRolesDto(UUID id,
                            String username,
                            String password,
                            Boolean enabled,
                            Boolean isProfileCreated,
                            List<String> roles,
                            Instant createdAt,
                            Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.isProfileCreated = isProfileCreated;
        this.roles = roles != null ? roles : List.of();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}

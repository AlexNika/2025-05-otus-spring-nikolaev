package ru.otus.hw.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * DTO for {@link ru.otus.hw.models.User}
 */
public record UserDto(Long id,
                      @Size(message = "Имя пользователя не может быть длиннее 255 символов")
                      @NotBlank(message = "Имя пользователя не может быть пустым")
                      String username,
                      @Size(message = "Пароль пользователя должен содержать от 5 до 255 символов", min = 5, max = 255)
                      @NotBlank(message = "Пароль пользователя не может быть пустым")
                      String password,
                      Boolean isActive,
                      Collection<? extends GrantedAuthority> roles) {
}
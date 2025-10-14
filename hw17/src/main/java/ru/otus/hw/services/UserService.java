package ru.otus.hw.services;

import ru.otus.hw.dto.UserDto;

import java.util.Optional;

public interface UserService {
    Optional<UserDto> findByUsername(String username);
}

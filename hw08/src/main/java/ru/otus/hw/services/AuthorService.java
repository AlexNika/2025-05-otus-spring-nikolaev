package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.AuthorDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorService {
    List<AuthorDto> findAll();

    List<AuthorDto> findByIds(Set<String> ids);

    Optional<AuthorDto> findById(String id);

    AuthorDto insert(@Valid String name);

    AuthorDto update(String id, @Valid String name);

    void deleteById(String id);
}

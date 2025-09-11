package ru.otus.hw.services;

import jakarta.validation.Valid;
import ru.otus.hw.dto.AuthorDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AuthorService {
    List<AuthorDto> findAll();

    List<AuthorDto> findByIds(Set<Long> ids);

    Optional<AuthorDto> findById(long id);

    AuthorDto insert(@Valid String name);

    AuthorDto update(long id, @Valid String name);

    void deleteById(Long id);
}

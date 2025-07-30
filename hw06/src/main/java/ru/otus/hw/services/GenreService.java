package ru.otus.hw.services;

import ru.otus.hw.dto.GenreDto;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreService {
    List<GenreDto> findAll();

    List<GenreDto> findByIds(Set<Long> ids);

    Optional<GenreDto> findById(Long id);

    GenreDto insert(String name);

    GenreDto update(Long id, String name);

    void deleteById(Long id);
}

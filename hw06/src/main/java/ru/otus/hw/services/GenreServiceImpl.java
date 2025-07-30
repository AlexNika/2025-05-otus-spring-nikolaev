package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mapper.GenreMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    private final GenreMapper mapper;

    @Override
    public List<GenreDto> findAll() {
        return genreRepository.findAll().stream().map(mapper::toGenreDto).toList();
    }

    @Override
    public List<GenreDto> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException("Genres id list must not be null or empty");
        }
        return genreRepository.findByIds(ids).stream().map(mapper::toGenreDto).toList();
    }

    @Override
    public Optional<GenreDto> findById(Long id) {
        return genreRepository.findById(id).map(mapper::toGenreDto);
    }

    @Override
    @Transactional
    public GenreDto insert(String name) {
        return mapper.toGenreDto(genreRepository.save(new Genre(0L, name)));
    }

    @Override
    @Transactional
    public GenreDto update(Long id, String name) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre with id %d not found".formatted(id)));
        genre.setName(name);
        return mapper.toGenreDto(genreRepository.save(genre));
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        genreRepository.deleteById(id);
    }
}

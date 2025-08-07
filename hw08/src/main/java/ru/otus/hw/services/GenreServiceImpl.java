package ru.otus.hw.services;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.dto.mappers.GenreMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;
import static ru.otus.hw.utils.ValidationMessages.ILLEGAL_ARGUMENT_MESSAGE;

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
    public List<GenreDto> findByIds(Set<String> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException(ILLEGAL_ARGUMENT_MESSAGE.getMessage(Genre.class.getSimpleName()));
        }
        return genreRepository.findAllById(ids).stream().map(mapper::toGenreDto).toList();
    }

    @Override
    public Optional<GenreDto> findById(String id) {
        return genreRepository.findById(id).map(mapper::toGenreDto);
    }

    @Override
    public GenreDto insert(@Valid String name) {
        return mapper.toGenreDto(genreRepository.save(new Genre(name)));
    }

    @Override
    public GenreDto update(String id, @Valid String name) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        genre.setName(name);
        return mapper.toGenreDto(genreRepository.save(genre));
    }

    @Override
    public void deleteById(String id) {
        genreRepository.deleteById(id);
    }
}

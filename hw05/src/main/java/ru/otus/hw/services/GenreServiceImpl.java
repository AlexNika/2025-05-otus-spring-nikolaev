package ru.otus.hw.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.GenreRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.springframework.util.CollectionUtils.isEmpty;

@RequiredArgsConstructor
@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    @Override
    public List<Genre> findAll() {
        return genreRepository.findAll();
    }

    @Override
    public List<Genre> findByIds(Set<Long> ids) {
        if (isEmpty(ids)) {
            throw new IllegalArgumentException("Genres ids must not be null or empty");
        }
        return genreRepository.findByIds(ids);
    }

    @Override
    public Optional<Genre> findById(long id) {
        return genreRepository.findById(id);
    }
}

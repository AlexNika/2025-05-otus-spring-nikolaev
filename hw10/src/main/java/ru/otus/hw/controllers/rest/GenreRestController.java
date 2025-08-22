package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenreRestController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<List<GenreDto>> getAllGenres() {
        List<GenreDto> genres = genreService.findAll();
        return ResponseEntity.ok(genres);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreDto> getGenre(@PathVariable("id") Long id) {
        GenreDto genre = genreService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Genre.class.getSimpleName(), id)));
        return ResponseEntity.ok(genre);
    }

    @PostMapping
    public ResponseEntity<GenreDto> createGenre(@Valid @RequestBody GenreDto genreDto) {
        GenreDto createdGenre = genreService.insert(genreDto.name());
        return ResponseEntity.ok(createdGenre);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreDto> updateGenre(@PathVariable("id") Long id,
                                                @Valid @RequestBody GenreDto genreDto) {
        GenreDto updatedGenre = genreService.update(id, genreDto.name());
        return ResponseEntity.ok(updatedGenre);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable("id") Long id) {
        genreService.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
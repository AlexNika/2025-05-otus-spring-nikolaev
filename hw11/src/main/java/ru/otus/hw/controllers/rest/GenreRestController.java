package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
@Validated
public class GenreRestController {

    private final GenreService genreService;

    @GetMapping
    public Flux<GenreDto> getAllGenres() {
        return genreService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<GenreDto> getGenreById(@PathVariable Long id) {
        return genreService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<GenreDto> createGenre(@Valid @RequestBody GenreCreateRequest request) {
        return genreService.insert(request.name());
    }

    @PutMapping("/{id}")
    public Mono<GenreDto> updateGenre(
            @PathVariable Long id,
            @Valid @RequestBody GenreUpdateRequest request
    ) {
        return genreService.update(id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteGenre(@PathVariable Long id) {
        return genreService.deleteById(id);
    }

    public record GenreCreateRequest(@Valid String name) {}

    public record GenreUpdateRequest(@Valid String name) {}
}
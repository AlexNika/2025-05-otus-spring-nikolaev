package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class GenreCommands {

    private final GenreService genreService;

    private final GenreConverter genreConverter;

    @ShellMethod(value = "Find all genres", key = "ag")
    public String findAllGenres() {
        return genreService.findAll()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Find genre by id", key = "gbid")
    public String findGenreById(Long id) {
        return genreService.findById(id)
                .map(genreConverter::genreToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Find genres by ids (example: gbids 1,2,3)", key = "gbids")
    public String findGenresByIds(Set<Long> ids) {
        return genreService.findByIds(ids)
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Insert genre", key = "gins")
    public String insertGenre(@Valid String name) {
        return genreService.insert(name)
                .map(genreConverter::genreToString)
                .block();
    }

    @ShellMethod(value = "Update genre", key = "gupd")
    public String updateGenre(Long id, @Valid String name) {
        return genreService.update(id, name)
                .map(genreConverter::genreToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Delete genre by id", key = "gdel")
    public void deleteGenreById(Long id) {
        genreService.deleteById(id).block();
    }
}
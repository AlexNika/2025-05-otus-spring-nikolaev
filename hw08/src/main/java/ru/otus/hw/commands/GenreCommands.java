package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.GenreDto;
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
        return genreService.findAll().stream()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find genres by id", key = "gbid")
    public String findGenresById(String id) {
        return genreService.findById(id)
                .map(genreConverter::genreToString)
                .orElse(ENTITY_NOT_FOUND_MESSAGE.getMessage(Genre.class.getSimpleName(), id));
    }

    @ShellMethod(value = "Find genres by ids (example: gbids 1,2,3)", key = "gbids")
    public String findGenresByIds(Set<String> ids) {
        return genreService.findByIds(ids).stream()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert genre", key = "gins")
    public String insertGenre(@Valid String name) {
        GenreDto genreDto = genreService.insert(name);
        return genreConverter.genreToString(genreDto);
    }

    @ShellMethod(value = "Update genre", key = "gupd")
    public String updateGenre(String id, @Valid String name) {
        GenreDto genreDto = genreService.update(id, name);
        return genreConverter.genreToString(genreDto);
    }

    @ShellMethod(value = "Delete genre by id", key = "gdel")
    public void deleteGenreById(String id) {
        genreService.deleteById(id);
    }
}

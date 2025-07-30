package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.GenreConverter;
import ru.otus.hw.dto.GenreDto;
import ru.otus.hw.services.GenreService;

import java.util.Set;
import java.util.stream.Collectors;

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
    public String findGenresById(Long id) {
        return genreService.findById(id)
                .map(genreConverter::genreToString)
                .orElse("Genre with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find genres by ids (example: gbids 1,2,3)", key = "gbids")
    public String findGenresByIds(Set<Long> ids) {
        return genreService.findByIds(ids).stream()
                .map(genreConverter::genreToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert genre", key = "gins")
    public String insertGenre(String name) {
        GenreDto genreDto = genreService.insert(name);
        return genreConverter.genreToString(genreDto);
    }

    @ShellMethod(value = "Update genre", key = "gupd")
    public String updateGenre(Long id, String name) {
        GenreDto genreDto = genreService.update(id, name);
        return genreConverter.genreToString(genreDto);
    }

    @ShellMethod(value = "Delete genre by id", key = "gdel")
    public void deleteGenreById(Long id) {
        genreService.deleteById(id);
    }
}

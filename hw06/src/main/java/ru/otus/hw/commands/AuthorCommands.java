package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.services.AuthorService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class AuthorCommands {

    private final AuthorService authorService;

    private final AuthorConverter authorConverter;

    @ShellMethod(value = "Find all authors", key = "aa")
    public String findAllAuthors() {
        return authorService.findAll().stream()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find author by id", key = "abid")
    public String findAuthorById(Long id) {
        return authorService.findById(id)
                .map(authorConverter::authorToString)
                .orElse("Author with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Find authors by ids (example: abids 1,2,3)", key = "abids")
    public String findAuthorsByIds(Set<Long> ids) {
        return authorService.findByIds(ids).stream()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert author", key = "ains")
    public String insertAuthor(String name) {
        AuthorDto authorDto = authorService.insert(name);
        return authorConverter.authorToString(authorDto);
    }

    @ShellMethod(value = "Update genre", key = "aupd")
    public String updateAuthor(Long id, String name) {
        AuthorDto authorDto = authorService.update(id, name);
        return authorConverter.authorToString(authorDto);
    }

    @ShellMethod(value = "Delete genre by id", key = "adel")
    public void deleteGenreById(Long id) {
        authorService.deleteById(id);
    }
}

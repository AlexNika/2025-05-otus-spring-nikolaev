package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.AuthorConverter;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.Set;
import java.util.stream.Collectors;

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

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
                .orElse(ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id));
    }

    @ShellMethod(value = "Find authors by ids (example: abids 1,2,3)", key = "abids")
    public String findAuthorsByIds(Set<Long> ids) {
        return authorService.findByIds(ids).stream()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Insert author", key = "ains")
    public String insertAuthor(@Valid String name) {
        AuthorDto authorDto = authorService.insert(name);
        return authorConverter.authorToString(authorDto);
    }

    @ShellMethod(value = "Update genre", key = "aupd")
    public String updateAuthor(Long id, @Valid String name) {
        AuthorDto authorDto = authorService.update(id, name);
        return authorConverter.authorToString(authorDto);
    }

    @ShellMethod(value = "Delete genre by id", key = "adel")
    public void deleteGenreById(Long id) {
        authorService.deleteById(id);
    }
}

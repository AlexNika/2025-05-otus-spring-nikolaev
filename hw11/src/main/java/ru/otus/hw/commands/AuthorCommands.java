package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.AuthorConverter;
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
        return authorService.findAll()
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Find author by id", key = "abid")
    public String findAuthorById(Long id) {
        return authorService.findById(id)
                .map(authorConverter::authorToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Find authors by ids (example: abids 1,2,3)", key = "abids")
    public String findAuthorsByIds(Set<Long> ids) {
        return authorService.findByIds(ids)
                .map(authorConverter::authorToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Insert author", key = "ains")
    public String insertAuthor(@Valid String fullName) {
        return authorService.insert(fullName)
                .map(authorConverter::authorToString)
                .block();
    }

    @ShellMethod(value = "Update author", key = "aupd")
    public String updateAuthor(Long id, @Valid String fullName) {
        return authorService.update(id, fullName)
                .map(authorConverter::authorToString)
                .switchIfEmpty(Mono.just(ENTITY_NOT_FOUND_MESSAGE.getMessage(Author.class.getSimpleName(), id)))
                .block();
    }

    @ShellMethod(value = "Delete author by id", key = "adel")
    public void deleteAuthorById(Long id) {
        authorService.deleteById(id).block();
    }
}
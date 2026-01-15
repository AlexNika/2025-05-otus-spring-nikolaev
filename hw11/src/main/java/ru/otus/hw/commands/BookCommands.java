package ru.otus.hw.commands;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import reactor.core.publisher.Mono;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class BookCommands {

    private final BookService bookService;

    private final BookConverter bookConverter;

    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        return bookService.findAll()
                .map(bookConverter::bookToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Find books by id", key = "bbid")
    public String findBookById(Long id) {
        return bookService.findById(id)
                .map(bookConverter::bookToString)
                .switchIfEmpty(Mono.just("Book with id %d not found".formatted(id)))
                .block();
    }

    @ShellMethod(value = "Find all books with comments", key = "abwc")
    public String findBookByCommentId() {
        return bookService.findAllWithGenresAndComments()
                .map(bookConverter::bookWithCommentsToString)
                .collect(Collectors.joining("," + System.lineSeparator()))
                .block();
    }

    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(@Valid String title, Long authorId, Set<Long> genresIds) {
        return bookService.insert(title, authorId, genresIds)
                .map(bookConverter::bookToString)
                .block();
    }

    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(Long id, @Valid String title, Long authorId, Set<Long> genresIds) {
        return bookService.update(id, title, authorId, genresIds)
                .map(bookConverter::bookToString)
                .block();
    }

    @ShellMethod(value = "Delete book by id", key = "bdel")
    public void deleteBook(Long id) {
        bookService.deleteById(id).block();
    }
}
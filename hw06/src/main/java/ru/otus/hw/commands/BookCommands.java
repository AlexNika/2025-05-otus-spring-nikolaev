package ru.otus.hw.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import ru.otus.hw.converters.BookConverter;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.services.BookService;

import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
@ShellComponent
@RequiredArgsConstructor
public class BookCommands {

    private final BookService bookService;

    private final BookConverter bookConverter;

    private final BookMapper bookMapper;

    @ShellMethod(value = "Find all books", key = "ab")
    public String findAllBooks() {
        return bookService.findAll().stream()
                .map(bookConverter::bookToString)
                .collect(Collectors.joining("," + System.lineSeparator()));
    }

    @ShellMethod(value = "Find book by id", key = "bbid")
    public String findBookById(Long id) {
        return bookService.findById(id)
                .map(bookConverter::bookToString)
                .orElse("Book with id %d not found".formatted(id));
    }

    @ShellMethod(value = "Insert book", key = "bins")
    public String insertBook(String title, Long authorId, Set<Long> genresIds) {
        var savedBook = bookService.insert(title, authorId, genresIds);
        return bookConverter.bookToString(savedBook);
    }

    @ShellMethod(value = "Update book", key = "bupd")
    public String updateBook(Long id, String title, Long authorId, Set<Long> genresIds) {
        var savedBook = bookService.update(id, title, authorId, genresIds);
        return bookConverter.bookToString(savedBook);
    }

    @ShellMethod(value = "Delete book by id", key = "bdel")
    public void deleteBook(Long id) {
        bookService.deleteById(id);
    }
}

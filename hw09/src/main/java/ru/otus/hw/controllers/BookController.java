package ru.otus.hw.controllers;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.dto.CreateUpdateBookDto;
import ru.otus.hw.dto.mapper.BookMapper;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.CommentService;
import ru.otus.hw.services.GenreService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static java.util.Objects.isNull;
import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    private final AuthorService authorService;

    private final GenreService genreService;

    private final CommentService commentService;

    private final BookMapper bookMapper;

    @GetMapping("/books")
    public String getAllBooks(Model model) {
        List<BookDto> books = bookService.findAll();
        model.addAttribute("books", books);
        return "books";
    }

    @GetMapping("/books/{id}/details")
    public String getBook(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                          @PathVariable("id") Long id,
                          Model model) {
        BookDto book = bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), id)));
        List<CommentDto> comments = commentService.findByBookId(book.id());
        prepareModelData(model, book, comments, new ModelAttributes(referrer, null, null));
        return "book-view";
    }

    @GetMapping({"/books/add", "/books/{id}/edit"})
    public String showBookForm(@PathVariable(required = false) Long id,
                               @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                               Model model) {
        FormConfig config = prepareFormConfig(id);

        CreateUpdateBookDto book = config.isUpdate()
                ? bookMapper.toCreateUpdateBookDto(bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), id))))
                : CreateUpdateBookDto.builder().genreIds(new ArrayList<>()).build();
        prepareModelData(model, book, null,
                new ModelAttributes(referrer, config.formAction(), config.formTitle()));
        return "book-upsert";
    }

    @PostMapping({"/books/add", "/books/{id}/edit"})
    public String saveOrUpdateBook(
            @PathVariable(required = false) Long id,
            @Valid @ModelAttribute("book") CreateUpdateBookDto book,
            BindingResult bindingResult,
            @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
            Model model) {
        FormConfig config = prepareFormConfig(id);
        String action = config.isUpdate() ? "updating existing" : "saving new";
        ModelAttributes attributes = new ModelAttributes(referrer, config.formAction(), config.formTitle());
        if (bindingResult.hasErrors()) {
            log.error("An error occurred while {} book: {}", action, bindingResult.getFieldError());
            prepareModelData(model, book, null, attributes);
            return "book-upsert";
        }
        try {
            saveBook(id, book, config);
            return "redirect:/books";
        } catch (Exception e) {
            log.error("An error occurred while {} book: {}", action, e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при " + (config.isUpdate() ? "обновлении" : "сохранении") + " книги: " + e.getMessage());
            prepareModelData(model, book, null, attributes);
            return "book-upsert";
        }
    }

    @DeleteMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private void saveBook(Long id, CreateUpdateBookDto book, FormConfig config) {
        if (config.isUpdate()) {
            bookService.update(id, book.title(), book.authorId(), new HashSet<>(book.genreIds()));
        } else {
            bookService.insert(book.title(), book.authorId(), new HashSet<>(book.genreIds()));
        }
    }

    private <T> void prepareModelData(Model model, T book, @Nullable List<CommentDto> comments,
                                      ModelAttributes attributes) {
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        if (!isNull(comments)) {
            model.addAttribute("comments", comments);
        }
        model.addAttribute("previousUrl", attributes.referrer());
        if (!isNull(attributes.formAction())) {
            model.addAttribute("formAction", attributes.formAction());
        }
        if (!isNull(attributes.formTitle())) {
            model.addAttribute("formTitle", attributes.formTitle());
        }
    }

    private FormConfig prepareFormConfig(@Nullable Long id) {
        boolean isUpdate = id != null;
        String formAction = isUpdate ? "/books/" + id + "/edit" : "/books/add";
        String formTitle = isUpdate ? "Редактирование книги" : "Создание новой книги";
        return new FormConfig(formAction, formTitle, isUpdate);
    }

    private record ModelAttributes(String referrer, @Nullable String formAction, @Nullable String formTitle) {
    }

    private record FormConfig(String formAction, String formTitle, boolean isUpdate) {
    }
}

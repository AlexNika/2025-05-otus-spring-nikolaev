package ru.otus.hw.controllers.page;

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

import static ru.otus.hw.utils.ValidationMessages.ENTITY_NOT_FOUND_MESSAGE;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BookPageController {

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
        prepareBookDetailsModel(model, book, comments, referrer);
        return "book-view";
    }

    @GetMapping("/books/add")
    public String showAddBookForm(@RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                  Model model) {
        CreateUpdateBookDto book = CreateUpdateBookDto.builder().genreIds(new ArrayList<>()).build();
        prepareBookFormModel(model, book, referrer, "/books/add", "Создание новой книги");
        return "book-upsert";
    }

    @GetMapping("/books/{id}/edit")
    public String showEditBookForm(@PathVariable Long id,
                                   @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                                   Model model) {
        CreateUpdateBookDto book = bookMapper.toCreateUpdateBookDto(bookService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ENTITY_NOT_FOUND_MESSAGE
                        .getMessage(Book.class.getSimpleName(), id))));
        prepareBookFormModel(model, book, referrer, "/books/" + id + "/edit", "Редактирование книги");
        return "book-upsert";
    }

    @PostMapping("/books/add")
    public String createBook(@Valid @ModelAttribute("book") CreateUpdateBookDto book,
                             BindingResult bindingResult,
                             @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                             Model model) {
        if (bindingResult.hasErrors()) {
            log.error("An error occurred while saving new book: {}", bindingResult.getFieldError());
            prepareBookFormModel(model, book, referrer, "/books/add", "Создание новой книги");
            return "book-upsert";
        }
        try {
            bookService.insert(book.title(), book.authorId(), new HashSet<>(book.genreIds()));
            return "redirect:/books";
        } catch (Exception e) {
            log.error("An error occurred while saving new book: {}", e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при сохранении книги: " + e.getMessage());
            prepareBookFormModel(model, book, referrer, "/books/add", "Создание новой книги");
            return "book-upsert";
        }
    }

    @PostMapping("/books/{id}/edit")
    public String updateBook(@PathVariable Long id,
                             @Valid @ModelAttribute("book") CreateUpdateBookDto book,
                             BindingResult bindingResult,
                             @RequestHeader(value = HttpHeaders.REFERER, required = false) final String referrer,
                             Model model) {
        if (bindingResult.hasErrors()) {
            log.error("An error occurred while updating existing book: {}", bindingResult.getFieldError());
            prepareBookFormModel(model, book, referrer, "/books/" + id + "/edit",
                    "Редактирование книги");
            return "book-upsert";
        }
        try {
            bookService.update(id, book.title(), book.authorId(), new HashSet<>(book.genreIds()));
            return "redirect:/books";
        } catch (Exception e) {
            log.error("An error occurred while updating existing book: {}", e.getMessage());
            model.addAttribute("errorMessage",
                    "Ошибка при обновлении книги: " + e.getMessage());
            prepareBookFormModel(model, book, referrer, "/books/" + id + "/edit",
                    "Редактирование книги");
            return "book-upsert";
        }
    }

    @DeleteMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable("id") Long id) {
        bookService.deleteById(id);
        return "redirect:/books";
    }

    private void prepareBookDetailsModel(Model model, BookDto book, List<CommentDto> comments, String previousUrl) {
        model.addAttribute("book", book);
        model.addAttribute("comments", comments);
        model.addAttribute("previousUrl", previousUrl);
        model.addAttribute("id", book.id());
    }

    private void prepareBookFormModel(Model model, CreateUpdateBookDto book, String previousUrl, String formAction,
                                      String formTitle) {
        model.addAttribute("book", book);
        model.addAttribute("authors", authorService.findAll());
        model.addAttribute("genres", genreService.findAll());
        model.addAttribute("previousUrl", previousUrl);
        model.addAttribute("formAction", formAction);
        model.addAttribute("formTitle", formTitle);
    }
}
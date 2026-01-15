package ru.otus.hw.controllers.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.AuthorDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookService;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Validated
public class AuthorRestController {

    private final AuthorService authorService;

    private final BookService bookService;

    @GetMapping
    public Flux<AuthorDto> getAllAuthors() {
        return authorService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<AuthorDto> getAuthorById(@PathVariable long id) {
        return authorService.findById(id);
    }

    @GetMapping("/{id}/books")
    public Flux<BookDto> getBooksByAuthorId(@PathVariable long id) {
        return bookService.findBooksByAuthorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthorDto> createAuthor(@Valid @RequestBody AuthorCreateRequest request) {
        return authorService.insert(request.fullName());
    }

    @PutMapping("/{id}")
    public Mono<AuthorDto> updateAuthor(
            @PathVariable long id,
            @Valid @RequestBody AuthorUpdateRequest request
    ) {
        return authorService.update(id, request.fullName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteAuthor(@PathVariable Long id) {
        return authorService.deleteById(id);
    }

    public record AuthorCreateRequest(@Valid String fullName) {}

    public record AuthorUpdateRequest(@Valid String fullName) {}
}